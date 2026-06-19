package com.example.hr.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.approval.domain.MemberState;
import com.example.hr.approval.domain.StepType;
import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.approval.service.ApprovalService.LineMemberSpec;
import com.example.hr.signature.service.SignatureValidationService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** AP-001/002/010/034: 결재 상신·승인·반려 서비스(Mockito, DB 불필요). */
class ApprovalServiceTest {

	private ApprovalDocumentRepository documentRepository;
	private ApprovalLineSnapshotRepository lineRepository;
	private com.example.hr.approval.repository.DelegationRepository delegationRepository;
	private com.example.hr.approval.repository.MandateRepository mandateRepository;
	private SignatureValidationService signatureValidationService;
	private com.example.hr.notification.service.NotificationService notificationService;
	private ApprovalService service;

	@BeforeEach
	void setUp() {
		documentRepository = mock(ApprovalDocumentRepository.class);
		lineRepository = mock(ApprovalLineSnapshotRepository.class);
		delegationRepository = mock(com.example.hr.approval.repository.DelegationRepository.class);
		mandateRepository = mock(com.example.hr.approval.repository.MandateRepository.class);
		signatureValidationService = mock(SignatureValidationService.class);
		notificationService = mock(com.example.hr.notification.service.NotificationService.class);
		when(delegationRepository.findByActiveTrue()).thenReturn(java.util.List.of());
		when(mandateRepository.findByActiveTrue()).thenReturn(java.util.List.of());
		when(lineRepository.save(any(ApprovalLineSnapshot.class)))
			.thenAnswer(inv -> inv.getArgument(0));
		service = new ApprovalService(documentRepository, lineRepository, delegationRepository,
			mandateRepository, signatureValidationService, notificationService,
			Clock.fixed(Instant.parse("2026-06-19T00:00:00Z"), ZoneOffset.UTC));
	}

	private ApprovalDocument inProgressDoc() {
		ApprovalDocument doc = new ApprovalDocument(FormType.GENERAL, "제목", 10L, 20L);
		doc.changeStatus(DocumentStatus.IN_PROGRESS);
		return doc;
	}

	private ApprovalLineSnapshot member(int stepNo, Long approverId) {
		return new ApprovalLineSnapshot(1L, 1, stepNo, approverId, StepType.SEQUENTIAL);
	}

	@Test
	void AP002_상신시_결재선_스냅샷_생성_진행중() {
		when(documentRepository.save(any(ApprovalDocument.class))).thenAnswer(inv -> inv.getArgument(0));

		ApprovalDocument doc = service.submit(10L, 20L, FormType.GENERAL, "제목", List.of(
			new LineMemberSpec(1, 100L, StepType.SEQUENTIAL),
			new LineMemberSpec(2, 200L, StepType.SEQUENTIAL)));

		assertThat(doc.getStatus()).isEqualTo(DocumentStatus.IN_PROGRESS);
		verify(lineRepository, times(2)).save(any(ApprovalLineSnapshot.class));
	}

	@Test
	void AP001_빈_결재선_상신_거부() {
		assertThatThrownBy(() -> service.submit(10L, 20L, FormType.GENERAL, "제목", List.of()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP034_서명없는_승인_거부() {
		assertThatThrownBy(() -> service.approve(1L, 100L, null, null))
			.isInstanceOf(IllegalArgumentException.class);
		verify(documentRepository, never()).findById(anyLong());
	}

	@Test
	void AP010_AC1_차례아닌_단계_승인_거부() {
		ApprovalDocument doc = inProgressDoc();
		List<ApprovalLineSnapshot> line = List.of(member(1, 100L), member(2, 200L));
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);

		// 2단계 결재자(200)가 1단계 미완료 상태에서 승인 시도
		assertThatThrownBy(() -> service.approve(1L, 200L, 5L, "sig"))
			.isInstanceOf(IllegalStateException.class);
		verify(signatureValidationService, never()).verify(anyLong(), any(), any());
	}

	@Test
	void AP010_AC2_순차_전원승인_승인완료() {
		ApprovalDocument doc = inProgressDoc();
		ApprovalLineSnapshot s1 = member(1, 100L);
		s1.act(MemberState.APPROVED, java.time.OffsetDateTime.now(ZoneOffset.UTC), 100L); // 1단계 이미 승인
		ApprovalLineSnapshot s2 = member(2, 200L);
		List<ApprovalLineSnapshot> line = List.of(s1, s2);
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);
		when(signatureValidationService.verify(eq(5L), any(), eq("sig"))).thenReturn(true);

		DocumentStatus status = service.approve(1L, 200L, 5L, "sig");

		assertThat(status).isEqualTo(DocumentStatus.APPROVED);
		assertThat(s2.getState()).isEqualTo(MemberState.APPROVED);
	}

	@Test
	void FND008_서명검증실패시_승인거부() {
		ApprovalDocument doc = inProgressDoc();
		List<ApprovalLineSnapshot> line = List.of(member(1, 100L));
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);
		when(signatureValidationService.verify(anyLong(), any(), any())).thenReturn(false);

		assertThatThrownBy(() -> service.approve(1L, 100L, 5L, "bad-sig"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP031_반려_사유필수() {
		assertThatThrownBy(() -> service.reject(1L, 100L, "  "))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP031_반려시_문서_반려상태() {
		ApprovalDocument doc = inProgressDoc();
		List<ApprovalLineSnapshot> line = List.of(member(1, 100L));
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);

		DocumentStatus status = service.reject(1L, 100L, "보완 필요");

		assertThat(status).isEqualTo(DocumentStatus.REJECTED);
	}

	@Test
	void AP033_AC1_종결상태_문서_승인거부() {
		ApprovalDocument doc = inProgressDoc();
		doc.changeStatus(DocumentStatus.APPROVED); // 이미 승인완료
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));

		assertThatThrownBy(() -> service.approve(1L, 100L, 5L, "sig"))
			.isInstanceOf(IllegalStateException.class);
		verify(signatureValidationService, never()).verify(anyLong(), any(), any());
	}

	@Test
	void AP030_AC1_무승인_회수성공() {
		ApprovalDocument doc = inProgressDoc(); // 상신자 10L
		List<ApprovalLineSnapshot> line = List.of(member(1, 100L), member(2, 200L)); // 전부 PENDING
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);

		DocumentStatus status = service.withdraw(1L, 10L);

		assertThat(status).isEqualTo(DocumentStatus.WITHDRAWN);
	}

	@Test
	void AP030_AC2_승인이력있으면_회수거부() {
		ApprovalDocument doc = inProgressDoc();
		ApprovalLineSnapshot s1 = member(1, 100L);
		s1.act(MemberState.APPROVED, java.time.OffsetDateTime.now(ZoneOffset.UTC), 100L);
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(List.of(s1));

		assertThatThrownBy(() -> service.withdraw(1L, 10L))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void AP030_상신자아니면_회수거부() {
		ApprovalDocument doc = inProgressDoc(); // 상신자 10L
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));

		assertThatThrownBy(() -> service.withdraw(1L, 999L))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP032_AC1_반려문서_재상신_새라운드_진행중() {
		ApprovalDocument doc = inProgressDoc(); // 상신자 10L, round 1
		doc.changeStatus(DocumentStatus.REJECTED);
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));

		ApprovalDocument result = service.resubmit(1L, 10L, List.of(
			new LineMemberSpec(1, 100L, StepType.SEQUENTIAL)));

		assertThat(result.getStatus()).isEqualTo(DocumentStatus.IN_PROGRESS);
		assertThat(result.getCurrentRound()).isEqualTo(2); // 라운드 증가
		verify(lineRepository, times(1)).save(any(ApprovalLineSnapshot.class));
	}

	@Test
	void AP032_반려아닌_문서_재상신_거부() {
		ApprovalDocument doc = inProgressDoc(); // IN_PROGRESS
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));

		assertThatThrownBy(() -> service.resubmit(1L, 10L,
			List.of(new LineMemberSpec(1, 100L, StepType.SEQUENTIAL))))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void AP032_상신자아니면_재상신_거부() {
		ApprovalDocument doc = inProgressDoc();
		doc.changeStatus(DocumentStatus.REJECTED);
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));

		assertThatThrownBy(() -> service.resubmit(1L, 999L,
			List.of(new LineMemberSpec(1, 100L, StepType.SEQUENTIAL))))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP021_대결_대리인_승인처리() {
		ApprovalDocument doc = inProgressDoc();
		ApprovalLineSnapshot s1 = member(1, 100L); // 원 결재자 100
		List<ApprovalLineSnapshot> line = List.of(s1);
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);
		// 100→300 대결 활성
		when(delegationRepository.findByActiveTrue()).thenReturn(
			List.of(new com.example.hr.approval.entity.Delegation(100L, 300L)));
		when(signatureValidationService.verify(anyLong(), any(), any())).thenReturn(true);

		DocumentStatus status = service.approve(1L, 300L, 7L, "sig"); // 대리인 300 처리

		assertThat(status).isEqualTo(DocumentStatus.APPROVED);
		assertThat(s1.getActedById()).isEqualTo(300L); // 실제 처리자 기록
		assertThat(s1.getApproverId()).isEqualTo(100L); // 원 결재자 보존
	}

	@Test
	void AP022_위임_수임자_승인처리() {
		ApprovalDocument doc = inProgressDoc();
		ApprovalLineSnapshot s1 = member(1, 100L);
		List<ApprovalLineSnapshot> line = List.of(s1);
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);
		when(mandateRepository.findByActiveTrue()).thenReturn(
			List.of(new com.example.hr.approval.entity.Mandate(100L, 400L)));
		when(signatureValidationService.verify(anyLong(), any(), any())).thenReturn(true);

		DocumentStatus status = service.approve(1L, 400L, 7L, "sig"); // 수임자 400 처리

		assertThat(status).isEqualTo(DocumentStatus.APPROVED);
		assertThat(s1.getActedById()).isEqualTo(400L);
	}

	@Test
	void AP021_무관자_승인_거부() {
		ApprovalDocument doc = inProgressDoc();
		List<ApprovalLineSnapshot> line = List.of(member(1, 100L));
		when(documentRepository.findWithLockById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);
		// 대결/위임 없음 → 999는 무관자

		assertThatThrownBy(() -> service.approve(1L, 999L, 7L, "sig"))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
