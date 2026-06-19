package com.example.hr.location.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 거점 사진(LOC-004, 다중). 바이트는 MinIO, DB에는 메타(objectKey·파일명·업로더)만 기록.
 * objectKey는 외부 미노출 — 조회는 백엔드 경유.
 */
@Entity
@Table(name = "location_photo")
public class LocationPhoto extends BaseEntity {

	@Column(name = "location_id", nullable = false)
	private Long locationId;

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

	protected LocationPhoto() {
	}

	public LocationPhoto(Long locationId, String fileName, String contentType, long sizeBytes,
			Long uploaderId, String objectKey) {
		this.locationId = locationId;
		this.fileName = fileName;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.uploaderId = uploaderId;
		this.objectKey = objectKey;
	}

	public Long getLocationId() {
		return locationId;
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
