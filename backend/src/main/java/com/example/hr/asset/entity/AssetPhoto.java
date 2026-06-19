package com.example.hr.asset.entity;

import com.example.hr.asset.domain.AssetPhotoOwnerType;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 비품 사진(AST-005, 다중). 개체(INDIVIDUAL) 또는 품목(ITEM)에 귀속. 바이트는 MinIO, DB에는 메타만.
 */
@Entity
@Table(name = "asset_photo")
public class AssetPhoto extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "owner_type", nullable = false)
	private AssetPhotoOwnerType ownerType;

	@Column(name = "owner_id", nullable = false)
	private Long ownerId;

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

	protected AssetPhoto() {
	}

	public AssetPhoto(AssetPhotoOwnerType ownerType, Long ownerId, String fileName,
			String contentType, long sizeBytes, Long uploaderId, String objectKey) {
		this.ownerType = ownerType;
		this.ownerId = ownerId;
		this.fileName = fileName;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.uploaderId = uploaderId;
		this.objectKey = objectKey;
	}

	public AssetPhotoOwnerType getOwnerType() {
		return ownerType;
	}

	public Long getOwnerId() {
		return ownerId;
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
