package com.example.hr.location.service;

import com.example.hr.common.storage.FileStorage;
import com.example.hr.location.entity.LocationPhoto;
import com.example.hr.location.repository.LocationPhotoRepository;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 거점 사진 관리(LOC-004, 다중). 바이트는 MinIO({@link FileStorage}), 메타는 DB.
 * 조회·다운로드는 백엔드 경유(presigned 미사용). 권한은 컨트롤러 RBAC(거점관리자 범위).
 */
@Service
public class LocationPhotoService {

	public record DownloadResult(LocationPhoto meta, InputStream stream) {
	}

	private final LocationPhotoRepository photoRepository;
	private final FileStorage fileStorage;

	public LocationPhotoService(LocationPhotoRepository photoRepository, FileStorage fileStorage) {
		this.photoRepository = photoRepository;
		this.fileStorage = fileStorage;
	}

	@Transactional
	public LocationPhoto upload(Long locationId, Long uploaderId, String fileName,
			String contentType, long size, InputStream data) {
		String objectKey = "location/" + locationId + "/" + UUID.randomUUID();
		fileStorage.put(objectKey, data, size, contentType);
		return photoRepository.save(
			new LocationPhoto(locationId, fileName, contentType, size, uploaderId, objectKey));
	}

	@Transactional(readOnly = true)
	public List<LocationPhoto> list(Long locationId) {
		return photoRepository.findByLocationId(locationId);
	}

	@Transactional(readOnly = true)
	public DownloadResult download(Long photoId) {
		LocationPhoto photo = photoRepository.findById(photoId)
			.orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));
		return new DownloadResult(photo, fileStorage.get(photo.getObjectKey()));
	}

	@Transactional
	public void delete(Long photoId) {
		LocationPhoto photo = photoRepository.findById(photoId)
			.orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다."));
		fileStorage.delete(photo.getObjectKey());
		photoRepository.delete(photo);
	}
}
