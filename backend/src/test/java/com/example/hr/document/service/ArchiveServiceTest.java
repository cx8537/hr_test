package com.example.hr.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.document.domain.DocumentSource;
import com.example.hr.document.domain.DocumentVisibility;
import com.example.hr.document.entity.ArchiveDocument;
import com.example.hr.document.repository.ArchiveDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** DOC-002: 결재 종결 문서 자동 보관(공개범위 매핑 포함). */
class ArchiveServiceTest {

	private ArchiveDocumentRepository archiveRepository;
	private ArchiveService archiveService;

	@BeforeEach
	void setUp() {
		archiveRepository = mock(ArchiveDocumentRepository.class);
		archiveService = new ArchiveService(archiveRepository);
		when(archiveRepository.save(any(ArchiveDocument.class)))
			.thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void DOC002_AC1_승인완료_자동보관_전사공개() {
		ArchiveDocument doc = archiveService.archiveFromApproval(7L, FormType.EXPENSE,
			DocumentStatus.APPROVED, "출장비");

		assertThat(doc.getSource()).isEqualTo(DocumentSource.APPROVAL);
		assertThat(doc.getVisibility()).isEqualTo(DocumentVisibility.PUBLIC);
		assertThat(doc.getRefDocumentId()).isEqualTo(7L);
	}

	@Test
	void DOC002_AC2_반려문서_관여자한정_보관() {
		ArchiveDocument doc = archiveService.archiveFromApproval(8L, FormType.GENERAL,
			DocumentStatus.REJECTED, "품의");

		assertThat(doc.getVisibility()).isEqualTo(DocumentVisibility.INVOLVED_ONLY);
	}

	@Test
	void 휴가_승인완료_관여자한정() {
		ArchiveDocument doc = archiveService.archiveFromApproval(9L, FormType.LEAVE,
			DocumentStatus.APPROVED, "연차");

		assertThat(doc.getVisibility()).isEqualTo(DocumentVisibility.INVOLVED_ONLY);
	}

	@Test
	void 진행중_문서는_보관_거부() {
		assertThatThrownBy(() -> archiveService.archiveFromApproval(10L, FormType.EXPENSE,
			DocumentStatus.IN_PROGRESS, "진행중"))
			.isInstanceOf(IllegalArgumentException.class);
		verify(archiveRepository, never()).save(any());
	}

	@Test
	void 일반_업로드_전사공개_보관() {
		ArchiveDocument doc = archiveService.upload("사내 규정", "rule.pdf", 3L, 100L);

		assertThat(doc.getSource()).isEqualTo(DocumentSource.UPLOAD);
		assertThat(doc.getVisibility()).isEqualTo(DocumentVisibility.PUBLIC);
		assertThat(doc.getFolderId()).isEqualTo(3L);
	}
}
