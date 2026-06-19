package com.example.hr.reservation.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** 자원 사진(RSV-001, 다중). 바이트는 MinIO, DB에는 메타만. 백엔드 경유 접근. */
@Entity
@Table(name = "resource_photo")
public class ResourcePhoto extends BaseEntity {

	@Column(name = "resource_id", nullable = false)
	private Long resourceId;

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

	protected ResourcePhoto() {
	}

	public ResourcePhoto(Long resourceId, String fileName, String contentType, long sizeBytes,
			Long uploaderId, String objectKey) {
		this.resourceId = resourceId;
		this.fileName = fileName;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.uploaderId = uploaderId;
		this.objectKey = objectKey;
	}

	public Long getResourceId() {
		return resourceId;
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
