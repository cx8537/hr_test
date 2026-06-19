package com.example.hr.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.approval.domain.FormType;
import com.example.hr.approval.domain.StepType;
import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.entity.Attachment;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.approval.repository.AttachmentRepository;
import com.example.hr.common.storage.FileStorage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/** AP-040: 첨부 업로드(상신자만·메타 기록)·다운로드(관여자만, 백엔드 경유). */
class AttachmentServiceTest {

	private AttachmentRepository attachmentRepository;
	private ApprovalDocumentRepository documentRepository;
	private ApprovalLineSnapshotRepository lineRepository;
	private FileStorage fileStorage;
	private AttachmentService attachmentService;

	@BeforeEach
	void setUp() {
		attachmentRepository = mock(AttachmentRepository.class);
		documentRepository = mock(ApprovalDocumentRepository.class);
		lineRepository = mock(ApprovalLineSnapshotRepository.class);
		fileStorage = mock(FileStorage.class);
		attachmentService = new AttachmentService(attachmentRepository, documentRepository,
			lineRepository, fileStorage);
	}

	private ApprovalDocument document(Long drafterId) {
		return new ApprovalDocument(FormType.GENERAL, "제목", drafterId, 9L);
	}

	@Test
	void AP040_AC1_업로드_메타기록_MinIO저장() {
		when(documentRepository.findById(1L)).thenReturn(Optional.of(document(100L)));
		when(attachmentRepository.save(any(Attachment.class))).thenAnswer(inv -> inv.getArgument(0));
		InputStream data = new ByteArrayInputStream("hi".getBytes());

		Attachment saved = attachmentService.upload(1L, 100L, "report.pdf", "application/pdf", 2L, data);

		assertThat(saved.getFileName()).isEqualTo("report.pdf");
		assertThat(saved.getUploaderId()).isEqualTo(100L);
		assertThat(saved.getObjectKey()).startsWith("1/");
		verify(fileStorage).put(anyString(), any(), eq(2L), eq("application/pdf"));
		verify(attachmentRepository).save(any(Attachment.class));
	}

	@Test
	void AP040_상신자아니면_업로드_거부() {
		when(documentRepository.findById(1L)).thenReturn(Optional.of(document(100L)));
		InputStream data = new ByteArrayInputStream("hi".getBytes());

		assertThatThrownBy(() ->
			attachmentService.upload(1L, 999L, "report.pdf", "application/pdf", 2L, data))
			.isInstanceOf(AccessDeniedException.class);
		verify(fileStorage, never()).put(anyString(), any(), anyLong(), anyString());
	}

	@Test
	void AP040_상신자_다운로드_허용() {
		Attachment attachment = new Attachment(1L, "report.pdf", "application/pdf", 2L, 100L, "1/k");
		when(attachmentRepository.findById(7L)).thenReturn(Optional.of(attachment));
		when(documentRepository.findById(1L)).thenReturn(Optional.of(document(100L)));
		when(fileStorage.get("1/k")).thenReturn(new ByteArrayInputStream("hi".getBytes()));

		var result = attachmentService.download(7L, 100L);

		assertThat(result.meta().getFileName()).isEqualTo("report.pdf");
		verify(fileStorage).get("1/k");
	}

	@Test
	void AP040_결재선멤버_다운로드_허용() {
		Attachment attachment = new Attachment(1L, "report.pdf", "application/pdf", 2L, 100L, "1/k");
		when(attachmentRepository.findById(7L)).thenReturn(Optional.of(attachment));
		when(documentRepository.findById(1L)).thenReturn(Optional.of(document(100L)));
		when(lineRepository.findByDocumentId(1L)).thenReturn(List.of(
			new ApprovalLineSnapshot(1L, 1, 1, 200L, StepType.SEQUENTIAL)));
		when(fileStorage.get("1/k")).thenReturn(new ByteArrayInputStream("hi".getBytes()));

		var result = attachmentService.download(7L, 200L);

		assertThat(result.meta().getFileName()).isEqualTo("report.pdf");
		verify(fileStorage).get("1/k");
	}

	@Test
	void AP040_비관여자_다운로드_거부() {
		Attachment attachment = new Attachment(1L, "report.pdf", "application/pdf", 2L, 100L, "1/k");
		when(attachmentRepository.findById(7L)).thenReturn(Optional.of(attachment));
		when(documentRepository.findById(1L)).thenReturn(Optional.of(document(100L)));
		when(lineRepository.findByDocumentId(1L)).thenReturn(List.of(
			new ApprovalLineSnapshot(1L, 1, 1, 200L, StepType.SEQUENTIAL)));

		assertThatThrownBy(() -> attachmentService.download(7L, 999L))
			.isInstanceOf(AccessDeniedException.class);
		verify(fileStorage, never()).get(anyString());
	}
}
