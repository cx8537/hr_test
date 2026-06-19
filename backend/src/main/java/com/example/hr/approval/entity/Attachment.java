package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 결재 첨부 메타데이터(AP-040 AC1). 실제 바이트는 MinIO에 저장하고 DB에는 파일명·크기·업로더·문서ID·객체키만 기록.
 * objectKey는 MinIO 내부 식별자로 외부에 노출하지 않는다(다운로드는 백엔드 경유).
 */
@Entity
@Table(name = "attachment")
public class Attachment extends BaseEntity {

	@Column(name = "document_id", nullable = false)
	private Long documentId;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "size_bytes", nullable = false)
	private long sizeBytes;

	@Column(name = "uploader_id", nullable = false)
	private Long uploaderId;

	@Column(name = "object_key", nullable = false, unique = true)
	private String objectKey;

	protected Attachment() {
	}

	public Attachment(Long documentId, String fileName, String contentType, long sizeBytes,
			Long uploaderId, String objectKey) {
		this.documentId = documentId;
		this.fileName = fileName;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.uploaderId = uploaderId;
		this.objectKey = objectKey;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public long getSizeBytes() {
		return sizeBytes;
	}

	public Long getUploaderId() {
		return uploaderId;
	}

	public String getObjectKey() {
		return objectKey;
	}
}
