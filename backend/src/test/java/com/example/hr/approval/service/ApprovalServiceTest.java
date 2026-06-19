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
	private SignatureValidationService signatureValidationService;
	private ApprovalService service;

	@BeforeEach
	void setUp() {
		documentRepository = mock(ApprovalDocumentRepository.class);
		lineRepository = mock(ApprovalLineSnapshotRepository.class);
		signatureValidationService = mock(SignatureValidationService.class);
		service = new ApprovalService(documentRepository, lineRepository, signatureValidationService,
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
		when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
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
		s1.act(MemberState.APPROVED, java.time.OffsetDateTime.now(ZoneOffset.UTC)); // 1단계 이미 승인
		ApprovalLineSnapshot s2 = member(2, 200L);
		List<ApprovalLineSnapshot> line = List.of(s1, s2);
		when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
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
		when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
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
		when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));
		when(lineRepository.findByDocumentIdAndRoundOrderByStepNoAsc(1L, 1)).thenReturn(line);

		DocumentStatus status = service.reject(1L, 100L, "보완 필요");

		assertThat(status).isEqualTo(DocumentStatus.REJECTED);
	}
}
