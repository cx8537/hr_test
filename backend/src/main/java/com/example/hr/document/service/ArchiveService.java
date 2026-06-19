package com.example.hr.document.service;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.document.domain.DocumentSource;
import com.example.hr.document.domain.DocumentVisibility;
import com.example.hr.document.domain.VisibilityMapper;
import com.example.hr.document.entity.ArchiveDocument;
import com.example.hr.document.repository.ArchiveDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문서 아카이브(DOC-001/002/005). 결재 종결(승인완료/반려/회수) 시 자동 보관하며, 공개범위는
 * {@link VisibilityMapper}로 양식·상태에 따라 결정한다. 결합도 최소화를 위해 결재 모듈이 이 메서드를
 * (도메인 이벤트 핸들러 등으로) 호출하도록 한다.
 */
@Service
public class ArchiveService {

	private final ArchiveDocumentRepository archiveRepository;

	public ArchiveService(ArchiveDocumentRepository archiveRepository) {
		this.archiveRepository = archiveRepository;
	}

	/** 결재 종결 문서 자동 보관(DOC-002). 진행 중 상태는 보관 대상이 아니다. */
	@Transactional
	public ArchiveDocument archiveFromApproval(Long refDocumentId, FormType formType,
			DocumentStatus status, String title) {
		if (!isTerminal(status)) {
			throw new IllegalArgumentException("종결된 결재 문서만 보관할 수 있습니다.");
		}
		DocumentVisibility visibility = VisibilityMapper.forApproval(formType, status);
		return archiveRepository.save(new ArchiveDocument(title, null, DocumentSource.APPROVAL,
			visibility, null, refDocumentId, null));
	}

	/** 일반 업로드 문서 등록(DOC-001). 공개범위는 전사공개. */
	@Transactional
	public ArchiveDocument upload(String title, String fileName, Long folderId, Long registrarId) {
		return archiveRepository.save(new ArchiveDocument(title, fileName, DocumentSource.UPLOAD,
			VisibilityMapper.forUpload(), folderId, null, registrarId));
	}

	private boolean isTerminal(DocumentStatus status) {
		return status == DocumentStatus.APPROVED
			|| status == DocumentStatus.REJECTED
			|| status == DocumentStatus.WITHDRAWN;
	}
}
