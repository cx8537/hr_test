package com.example.hr.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.hr.approval.domain.FormType;
import com.example.hr.approval.domain.StepType;
import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.common.storage.FileStorage;
import com.example.hr.document.domain.DocumentSource;
import com.example.hr.document.domain.DocumentVisibility;
import com.example.hr.document.entity.ArchiveDocument;
import com.example.hr.document.entity.ArchiveVersion;
import com.example.hr.document.repository.ArchiveDocumentRepository;
import com.example.hr.document.repository.ArchiveVersionRepository;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/** DOC-004/005/006/007: 검색·관여자 다운로드·비관여자 거부·버전 채번. */
class DocumentArchiveServiceTest {

	private ArchiveDocumentRepository archiveRepository;
	private ArchiveVersionRepository versionRepository;
	private ApprovalDocumentRepository approvalDocumentRepository;
	private ApprovalLineSnapshotRepository lineRepository;
	private AuthorizationService authorizationService;
	private FileStorage fileStorage;
	private DocumentArchiveService service;

	@BeforeEach
	void setUp() {
		archiveRepository = mock(ArchiveDocumentRepository.class);
		versionRepository = mock(ArchiveVersionRepository.class);
		approvalDocumentRepository = mock(ApprovalDocumentRepository.class);
		lineRepository = mock(ApprovalLineSnapshotRepository.class);
		authorizationService = mock(AuthorizationService.class);
		fileStorage = mock(FileStorage.class);
		service = new DocumentArchiveService(archiveRepository, versionRepository,
			approvalDocumentRepository, lineRepository, authorizationService, fileStorage);
	}

	/** 관여자한정 휴가 문서: 상신자 100, 결재자 200. */
	private ArchiveDocument leaveDoc() {
		return new ArchiveDocument("연차", null, DocumentSource.APPROVAL,
			DocumentVisibility.INVOLVED_ONLY, null, 7L, null);
	}

	private void stubInvolved() {
		ApprovalDocument approval = new ApprovalDocument(FormType.LEAVE, "연차", 100L, 9L);
		when(approvalDocumentRepository.findById(7L)).thenReturn(Optional.of(approval));
		when(lineRepository.findByDocumentId(7L)).thenReturn(List.of(
			new ApprovalLineSnapshot(7L, 1, 1, 200L, StepType.SEQUENTIAL)));
	}

	@Test
	void DOC005_AC1_관여자_다운로드_허용() {
		when(archiveRepository.findById(1L)).thenReturn(Optional.of(leaveDoc()));
		when(authorizationService.isAllowed(any(), any(AccessRequest.class))).thenReturn(false);
		stubInvolved();
		when(versionRepository.findByArchiveDocumentIdOrderByVersionNoDesc(1L)).thenReturn(List.of(
			new ArchiveVersion(1L, 1, "k", 100L)));
		when(fileStorage.get("k")).thenReturn(new ByteArrayInputStream("hi".getBytes()));

		var result = service.downloadLatest(1L, 200L); // 결재자 200

		assertThat(result.version().getVersionNo()).isEqualTo(1);
	}

	@Test
	void DOC005_AC1_비관여자_다운로드_거부() {
		when(archiveRepository.findById(1L)).thenReturn(Optional.of(leaveDoc()));
		when(authorizationService.isAllowed(any(), any(AccessRequest.class))).thenReturn(false);
		stubInvolved();

		assertThatThrownBy(() -> service.downloadLatest(1L, 999L)) // 비관여자
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void 시스템관리자는_관여자한정도_다운로드() {
		when(archiveRepository.findById(1L)).thenReturn(Optional.of(leaveDoc()));
		when(authorizationService.isAllowed(any(), any(AccessRequest.class))).thenReturn(true); // SYS_ADMIN
		when(versionRepository.findByArchiveDocumentIdOrderByVersionNoDesc(1L)).thenReturn(List.of(
			new ArchiveVersion(1L, 1, "k", 100L)));
		when(fileStorage.get("k")).thenReturn(new ByteArrayInputStream("hi".getBytes()));

		var result = service.downloadLatest(1L, 999L);

		assertThat(result.document().getTitle()).isEqualTo("연차");
	}

	@Test
	void DOC004_검색은_볼수없는_문서_제외() {
		ArchiveDocument leave = leaveDoc();
		ArchiveDocument publicDoc = new ArchiveDocument("규정", "r.pdf", DocumentSource.UPLOAD,
			DocumentVisibility.PUBLIC, null, null, 5L);
		when(archiveRepository
			.findByTitleContainingIgnoreCaseOrFileNameContainingIgnoreCase("문서", "문서"))
			.thenReturn(List.of(leave, publicDoc));
		when(authorizationService.isAllowed(any(), any(AccessRequest.class))).thenReturn(false);
		stubInvolved();

		var results = service.search("문서", 999L); // 비관여자 → 휴가 제외, 전사공개만

		assertThat(results).hasSize(1);
		assertThat(results.get(0).getVisibility()).isEqualTo(DocumentVisibility.PUBLIC);
	}

	@Test
	void DOC006_버전_채번_최신다음번호() {
		when(archiveRepository.findById(1L)).thenReturn(Optional.of(leaveDoc()));
		when(versionRepository.findByArchiveDocumentIdOrderByVersionNoDesc(1L)).thenReturn(List.of(
			new ArchiveVersion(1L, 2, "k2", 100L)));
		when(versionRepository.save(any(ArchiveVersion.class))).thenAnswer(inv -> inv.getArgument(0));

		ArchiveVersion v = service.addVersion(1L, 100L, "application/pdf", 3L,
			new ByteArrayInputStream("hi".getBytes()));

		assertThat(v.getVersionNo()).isEqualTo(3); // 기존 최신 2 → 3
	}
}
