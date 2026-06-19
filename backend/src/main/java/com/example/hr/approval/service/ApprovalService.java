package com.example.hr.approval.service;

import com.example.hr.approval.domain.ApprovalFlowEngine;
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
	private final SignatureValidationService signatureValidationService;
	private final Clock clock;

	public ApprovalService(ApprovalDocumentRepository documentRepository,
			ApprovalLineSnapshotRepository lineRepository,
			SignatureValidationService signatureValidationService, Clock clock) {
		this.documentRepository = documentRepository;
		this.lineRepository = lineRepository;
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

	/** 승인(AP-010/034): 차례 검증 후 서명을 검증하고 단계를 승인 처리, 문서 상태 재평가. */
	@Transactional
	public DocumentStatus approve(Long documentId, Long approverId, Long publicKeyId,
			String signatureBase64) {
		if (publicKeyId == null || signatureBase64 == null || signatureBase64.isBlank()) {
			throw new IllegalArgumentException("승인에는 서명이 필요합니다."); // AP-034
		}
		ApprovalDocument document = getDocument(documentId);
		List<ApprovalLineSnapshot> line = currentLine(documentId, document.getCurrentRound());
		ApprovalLineSnapshot target = requireActiveMember(line, approverId);

		byte[] payload = signaturePayload(documentId, document.getCurrentRound(), approverId);
		if (!signatureValidationService.verify(publicKeyId, payload, signatureBase64)) {
			throw new IllegalArgumentException("서명 검증에 실패했습니다."); // FND-008
		}
		target.act(MemberState.APPROVED, OffsetDateTime.now(clock));
		return reevaluate(document, line);
	}

	/** 반려(AP-031/012): 사유 필수, 차례 검증 후 반려 처리(병렬이면 즉시 전체 반려). */
	@Transactional
	public DocumentStatus reject(Long documentId, Long approverId, String reason) {
		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("반려 사유는 필수입니다."); // AP-031
		}
		ApprovalDocument document = getDocument(documentId);
		List<ApprovalLineSnapshot> line = currentLine(documentId, document.getCurrentRound());
		ApprovalLineSnapshot target = requireActiveMember(line, approverId);

		target.act(MemberState.REJECTED, OffsetDateTime.now(clock));
		return reevaluate(document, line);
	}

	private ApprovalDocument getDocument(Long documentId) {
		return documentRepository.findById(documentId)
			.orElseThrow(() -> new IllegalArgumentException("결재 문서를 찾을 수 없습니다."));
	}

	private List<ApprovalLineSnapshot> currentLine(Long documentId, int round) {
		return lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(documentId, round);
	}

	/** 결재 대기 중인 본인 단계를 찾고, 현재 차례(활성 단계)인지 검증(AP-010 AC1). */
	private ApprovalLineSnapshot requireActiveMember(List<ApprovalLineSnapshot> line, Long approverId) {
		ApprovalLineSnapshot target = line.stream()
			.filter(s -> s.getApproverId().equals(approverId) && s.getState() == MemberState.PENDING)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("결재 대상이 아닙니다."));

		List<Step> steps = toSteps(line);
		int stepIndex = distinctStepNos(line).indexOf(target.getStepNo());
		if (!ApprovalFlowEngine.isStepActive(steps, stepIndex)) {
			throw new IllegalStateException("현재 결재 차례가 아닙니다."); // AP-010 AC1
		}
		return target;
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
