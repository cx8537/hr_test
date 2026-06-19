package com.example.hr.asset.service;

import com.example.hr.asset.domain.AssetPhotoOwnerType;
import com.example.hr.asset.entity.AssetPhoto;
import com.example.hr.asset.repository.AssetPhotoRepository;
import com.example.hr.common.storage.FileStorage;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비품 사진 관리(AST-005, 다중). 바이트는 MinIO({@link FileStorage}), 메타는 DB. 백엔드 경유 접근.
 */
@Service
public class AssetPhotoService {

	public record DownloadResult(AssetPhoto meta, InputStream stream) {
	}

	private final AssetPhotoRepository photoRepository;
	private final FileStorage fileStorage;

	public AssetPhotoService(AssetPhotoRepository photoRepository, FileStorage fileStorage) {
		this.photoRepository = photoRepository;
		this.fileStorage = fileStorage;
	}

	@Transactional
	public AssetPhoto upload(AssetPhotoOwnerType ownerType, Long ownerId, Long uploaderId,
			String fileName, String contentType, long size, InputStream data) {
		String objectKey = "asset/" + ownerType + "/" + ownerId + "/" + UUID.randomUUID();
		fileStorage.put(objectKey, data, size, contentType);
		return photoRepository.save(
			new AssetPhoto(ownerType, ownerId, fileName, contentType, size, uploaderId, objectKey));
	}

	@Transactional(readOnly = true)
	public List<AssetPhoto> list(AssetPhotoOwnerType ownerType, Long ownerId) {
		return photoRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId);
	}

	@Transactional(readOnly = true)
	public DownloadResult download(Long photoId) {
		AssetPhoto photo = photoRepository.findById(photoId)
			.orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));
		return new DownloadResult(photo, fileStorage.get(photo.getObjectKey()));
	}
}
