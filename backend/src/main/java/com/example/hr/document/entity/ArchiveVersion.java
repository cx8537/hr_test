package com.example.hr.document.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 문서 버전(DOC-006). (문서ID, 버전번호) 모델. 재업로드 시 버전이 쌓이고 최신이 기본 표시.
 * 바이트는 MinIO, DB에는 메타(objectKey)만.
 */
@Entity
@Table(name = "archive_version")
public class ArchiveVersion extends BaseEntity {

	@Column(name = "archive_document_id", nullable = false)
	private Long archiveDocumentId;

	@Column(name = "version_no", nullable = false)
	private int versionNo;

	@Column(name = "object_key", nullable = false, unique = true)
	private String objectKey;

	@Column(name = "uploader_id", nullable = false)
	private Long uploaderId;

	protected ArchiveVersion() {
	}

	public ArchiveVersion(Long archiveDocumentId, int versionNo, String objectKey,
			Long uploaderId) {
		this.archiveDocumentId = archiveDocumentId;
		this.versionNo = versionNo;
		this.objectKey = objectKey;
		this.uploaderId = uploaderId;
	}

	public Long getArchiveDocumentId() {
		return archiveDocumentId;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public String getObjectKey() {
		return objectKey;
	}

	public Long getUploaderId() {
		return uploaderId;
	}
}
