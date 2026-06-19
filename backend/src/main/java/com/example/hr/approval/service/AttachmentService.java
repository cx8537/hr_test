package com.example.hr.approval.service;

import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.entity.Attachment;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.approval.repository.AttachmentRepository;
import com.example.hr.common.storage.FileStorage;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결재 첨부 업로드/다운로드(AP-040). 바이트는 MinIO({@link FileStorage}), 메타는 DB에 기록(AC1).
 * 다운로드는 presigned 미사용 — 백엔드가 관여자 권한을 검사한 뒤 스트림을 중계한다.
 */
@Service
public class AttachmentService {

	/** 다운로드 결과(메타 + 스트림). 스트림은 컨트롤러가 응답에 복사 후 닫는다. */
	public record DownloadResult(Attachment meta, InputStream stream) {
	}

	private final AttachmentRepository attachmentRepository;
	private final ApprovalDocumentRepository documentRepository;
	private final ApprovalLineSnapshotRepository lineRepository;
	private final FileStorage fileStorage;

	public AttachmentService(AttachmentRepository attachmentRepository,
			ApprovalDocumentRepository documentRepository,
			ApprovalLineSnapshotRepository lineRepository, FileStorage fileStorage) {
		this.attachmentRepository = attachmentRepository;
		this.documentRepository = documentRepository;
		this.lineRepository = lineRepository;
		this.fileStorage = fileStorage;
	}

	/** 첨부 업로드(AP-040): 상신자만 첨부 가능. MinIO 저장 후 메타 기록. */
	@Transactional
	public Attachment upload(Long documentId, Long uploaderId, String fileName, String contentType,
			long size, InputStream data) {
		ApprovalDocument document = documentRepository.findById(documentId)
			.orElseThrow(() -> new IllegalArgumentException("결재 문서를 찾을 수 없습니다."));
		if (!document.getDrafterId().equals(uploaderId)) {
			throw new AccessDeniedException("상신자만 첨부할 수 있습니다."); // AP-040
		}
		String objectKey = documentId + "/" + UUID.randomUUID();
		fileStorage.put(objectKey, data, size, contentType);
		return attachmentRepository.save(
			new Attachment(documentId, fileName, contentType, size, uploaderId, objectKey));
	}

	/** 문서 첨부 목록(관여자만). */
	@Transactional(readOnly = true)
	public List<Attachment> list(Long documentId, Long requesterId) {
		requireInvolved(documentId, requesterId);
		return attachmentRepository.findByDocumentId(documentId);
	}

	/** 첨부 다운로드(AP-040): 관여자 검사 후 스트림 중계(presigned 미사용). */
	@Transactional(readOnly = true)
	public DownloadResult download(Long attachmentId, Long requesterId) {
		Attachment attachment = attachmentRepository.findById(attachmentId)
			.orElseThrow(() -> new IllegalArgumentException("첨부를 찾을 수 없습니다."));
		requireInvolved(attachment.getDocumentId(), requesterId);
		return new DownloadResult(attachment, fileStorage.get(attachment.getObjectKey()));
	}

	/** 관여자(상신자 또는 결재선 멤버) 판정. 비관여자는 403. */
	private void requireInvolved(Long documentId, Long requesterId) {
		ApprovalDocument document = documentRepository.findById(documentId)
			.orElseThrow(() -> new IllegalArgumentException("결재 문서를 찾을 수 없습니다."));
		if (document.getDrafterId().equals(requesterId)) {
			return;
		}
		boolean isApprover = lineRepository.findByDocumentId(documentId).stream()
			.map(ApprovalLineSnapshot::getApproverId)
			.anyMatch(requesterId::equals);
		if (!isApprover) {
			throw new AccessDeniedException("문서 관여자만 접근할 수 있습니다."); // AP-040 권한 검사
		}
	}
}
