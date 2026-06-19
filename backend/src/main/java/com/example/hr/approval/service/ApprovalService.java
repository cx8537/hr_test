package com.example.hr.approval.service;

import com.example.hr.approval.domain.ApprovalFlowEngine;
import com.example.hr.approval.domain.DelegationResolver;
import com.example.hr.approval.domain.DelegationResolver.DelegationLink;
import com.example.hr.approval.domain.DelegationResolver.MandateLink;
import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.approval.domain.MemberState;
import com.example.hr.approval.domain.Step;
import com.example.hr.approval.domain.StepMember;
import com.example.hr.approval.domain.StepType;
import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.approval.repository.DelegationRepository;
import com.example.hr.approval.repository.MandateRepository;
import com.example.hr.signature.service.SignatureValidationService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결재 상신·승인·반려 서비스(AP-001/002/010~013/034). 순수 흐름 엔진을 조립한다.
 * 상신 시 결재선을 스냅샷으로 복사·고정하고, 승인에는 키 서명을 요구한다(AP-034).
 */
@Service
public class ApprovalService {

	/** 상신 결재선 한 멤버 사양(단계 번호, 결재자, 단계 유형). */
	public record LineMemberSpec(int stepNo, Long approverId, StepType stepType) {
	}

	private final ApprovalDocumentRepository documentRepository;
	private final ApprovalLineSnapshotRepository lineRepository;
	private final DelegationRepository delegationRepository;
	private final MandateRepository mandateRepository;
	private final SignatureValidationService signatureValidationService;
	private final Clock clock;

	public ApprovalService(ApprovalDocumentRepository documentRepository,
			ApprovalLineSnapshotRepository lineRepository,
			DelegationRepository delegationRepository,
			MandateRepository mandateRepository,
			SignatureValidationService signatureValidationService, Clock clock) {
		this.documentRepository = documentRepository;
		this.lineRepository = lineRepository;
		this.delegationRepository = delegationRepository;
		this.mandateRepository = mandateRepository;
		this.signatureValidationService = signatureValidationService;
		this.clock = clock;
	}

	/** 상신(AP-001/002): 결재선을 스냅샷으로 고정하고 진행중으로 전이. */
	@Transactional
	public ApprovalDocument submit(Long drafterId, Long draftDeptId, FormType formType, String title,
			List<LineMemberSpec> line) {
		if (line == null || line.isEmpty()) {
			throw new IllegalArgumentException("결재선이 비어 있습니다.");
		}
		ApprovalDocument document = new ApprovalDocument(formType, title, drafterId, draftDeptId);
		document.changeStatus(DocumentStatus.IN_PROGRESS);
		documentRepository.save(document);
		for (LineMemberSpec spec : line) {
			lineRepository.save(new ApprovalLineSnapshot(
				document.getId(), document.getCurrentRound(), spec.stepNo(), spec.approverId(),
				spec.stepType()));
		}
		return document;
	}

	/**
	 * 반려 문서 재상신(AP-032): 반려 문서를 같은 문서로 재상신한다. 라운드를 올려 결재선을
	 * 1단계부터 새로 생성하며(이전 라운드 스냅샷·서명은 보존·판정 제외 AC2/AC3), 진행중으로 재개.
	 */
	@Transactional
	public ApprovalDocument resubmit(Long documentId, Long requesterId, List<LineMemberSpec> line) {
		if (line == null || line.isEmpty()) {
			throw new IllegalArgumentException("결재선이 비어 있습니다.");
		}
		ApprovalDocument document = getLockedDocument(documentId);
		if (!document.getDrafterId().equals(requesterId)) {
			throw new IllegalArgumentException("상신자만 재상신할 수 있습니다.");
		}
		if (document.getStatus() != DocumentStatus.REJECTED) {
			throw new IllegalStateException("반려된 문서만 재상신할 수 있습니다."); // AP-032
		}
		document.nextRound(); // 새 라운드(이전 라운드 스냅샷은 보존)
		for (LineMemberSpec spec : line) {
			lineRepository.save(new ApprovalLineSnapshot(
				documentId, document.getCurrentRound(), spec.stepNo(), spec.approverId(),
				spec.stepType()));
		}
		document.changeStatus(DocumentStatus.IN_PROGRESS);
		return document;
	}

	/** 승인(AP-010/034): 락 조회·상태 재검증 후 차례·서명 검증, 단계 승인 처리, 문서 상태 재평가. */
	@Transactional
	public DocumentStatus approve(Long documentId, Long actorId, Long publicKeyId,
			String signatureBase64) {
		if (publicKeyId == null || signatureBase64 == null || signatureBase64.isBlank()) {
			throw new IllegalArgumentException("승인에는 서명이 필요합니다."); // AP-034
		}
		ApprovalDocument document = getLockedDocument(documentId);
		requireInProgress(document); // AP-033: 진행중에서만 처리(종결 상태 거부)
		List<ApprovalLineSnapshot> line = currentLine(documentId, document.getCurrentRound());
		ApprovalLineSnapshot target = requireProcessableMember(line, actorId);

		// 서명 대상은 원 결재자 단계 식별. 서명은 실제 처리자(actor) 본인 키로(AP-021/022).
		byte[] payload = signaturePayload(documentId, document.getCurrentRound(), target.getApproverId());
		if (!signatureValidationService.verify(publicKeyId, payload, signatureBase64)) {
			throw new IllegalArgumentException("서명 검증에 실패했습니다."); // FND-008
		}
		target.act(MemberState.APPROVED, OffsetDateTime.now(clock), actorId);
		return reevaluate(document, line);
	}

	/** 반려(AP-031/012): 사유 필수, 차례 검증 후 반려 처리(병렬이면 즉시 전체 반려). */
	@Transactional
	public DocumentStatus reject(Long documentId, Long actorId, String reason) {
		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("반려 사유는 필수입니다."); // AP-031
		}
		ApprovalDocument document = getLockedDocument(documentId);
		requireInProgress(document); // AP-033
		List<ApprovalLineSnapshot> line = currentLine(documentId, document.getCurrentRound());
		ApprovalLineSnapshot target = requireProcessableMember(line, actorId);

		target.act(MemberState.REJECTED, OffsetDateTime.now(clock), actorId);
		return reevaluate(document, line);
	}

	/** 회수(AP-030): 상신자만, 누구도 승인하지 않은 경우만 가능. 보류 중에도 회수 가능(AP-013). */
	@Transactional
	public DocumentStatus withdraw(Long documentId, Long requesterId) {
		ApprovalDocument document = getLockedDocument(documentId);
		if (!document.getDrafterId().equals(requesterId)) {
			throw new IllegalArgumentException("상신자만 회수할 수 있습니다.");
		}
		if (document.getStatus() != DocumentStatus.IN_PROGRESS
			&& document.getStatus() != DocumentStatus.ON_HOLD) {
			throw new IllegalStateException("진행 중/보류 문서만 회수할 수 있습니다."); // AP-033 AC1
		}
		List<ApprovalLineSnapshot> line = currentLine(documentId, document.getCurrentRound());
		boolean anyApproved = line.stream().anyMatch(s -> s.getState() == MemberState.APPROVED);
		if (anyApproved) {
			throw new IllegalStateException("이미 승인된 결재는 회수할 수 없습니다."); // AP-030 AC2
		}
		document.changeStatus(DocumentStatus.WITHDRAWN);
		return DocumentStatus.WITHDRAWN;
	}

	private ApprovalDocument getLockedDocument(Long documentId) {
		return documentRepository.findWithLockById(documentId)
			.orElseThrow(() -> new IllegalArgumentException("결재 문서를 찾을 수 없습니다."));
	}

	/** 상태 재검증(AP-033): 진행 중 문서만 승인/반려 처리 허용. */
	private void requireInProgress(ApprovalDocument document) {
		if (document.getStatus() != DocumentStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중 문서만 결재할 수 있습니다.");
		}
	}

	private List<ApprovalLineSnapshot> currentLine(Long documentId, int round) {
		return lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(documentId, round);
	}

	/**
	 * actor가 처리 가능한(본인 또는 유효 대결/위임) 대기 단계를 찾고 현재 차례인지 검증한다.
	 * 처리 가능 단계가 있으나 차례가 아니면 IllegalState(AP-010 AC1), 처리 권한 자체가 없으면 IllegalArgument.
	 */
	private ApprovalLineSnapshot requireProcessableMember(List<ApprovalLineSnapshot> line, Long actorId) {
		List<DelegationLink> delegations = delegationRepository.findByActiveTrue().stream()
			.map(d -> new DelegationLink(d.getApproverId(), d.getDeputyId())).toList();
		List<MandateLink> mandates = mandateRepository.findByActiveTrue().stream()
			.map(m -> new MandateLink(m.getMandatorId(), m.getMandateeId())).toList();

		List<Step> steps = toSteps(line);
		List<Integer> stepNos = distinctStepNos(line);
		ApprovalLineSnapshot processableButNotActive = null;
		for (ApprovalLineSnapshot member : line) {
			if (member.getState() != MemberState.PENDING) {
				continue;
			}
			if (!DelegationResolver.resolve(member.getApproverId(), actorId, delegations, mandates)
				.allowed()) {
				continue;
			}
			processableButNotActive = member;
			if (ApprovalFlowEngine.isStepActive(steps, stepNos.indexOf(member.getStepNo()))) {
				return member;
			}
		}
		if (processableButNotActive != null) {
			throw new IllegalStateException("현재 결재 차례가 아닙니다."); // AP-010 AC1
		}
		throw new IllegalArgumentException("결재 대상이 아닙니다.");
	}

	private DocumentStatus reevaluate(ApprovalDocument document, List<ApprovalLineSnapshot> line) {
		DocumentStatus status = ApprovalFlowEngine.evaluate(toSteps(line));
		document.changeStatus(status);
		return status;
	}

	private List<Step> toSteps(List<ApprovalLineSnapshot> line) {
		Map<Integer, List<ApprovalLineSnapshot>> byStep = line.stream()
			.collect(Collectors.groupingBy(ApprovalLineSnapshot::getStepNo, TreeMap::new,
				Collectors.toList()));
		return byStep.values().stream()
			.map(members -> new Step(members.get(0).getStepType(),
				members.stream().map(m -> new StepMember(m.getState())).toList()))
			.toList();
	}

	private List<Integer> distinctStepNos(List<ApprovalLineSnapshot> line) {
		return line.stream().map(ApprovalLineSnapshot::getStepNo).distinct().sorted().toList();
	}

	private byte[] signaturePayload(Long documentId, int round, Long approverId) {
		return ("doc:" + documentId + ":r" + round + ":" + approverId).getBytes(StandardCharsets.UTF_8);
	}
}
