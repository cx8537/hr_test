package com.example.hr.common.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MinIO 기반 파일 저장(AP-040). 자격증명 확보 후 {@code app.minio.enabled=true}일 때만 빈 등록.
 * presigned URL을 발급하지 않고 스트림을 백엔드가 직접 중계한다(CLAUDE.md 절대 규칙).
 */
@Component
@ConditionalOnProperty(prefix = "app.minio", name = "enabled", havingValue = "true")
public class MinioFileStorage implements FileStorage {

	private final MinioClient client;
	private final String bucket;

	public MinioFileStorage(@Value("${app.minio.endpoint}") String endpoint,
			@Value("${app.minio.access-key}") String accessKey,
			@Value("${app.minio.secret-key}") String secretKey,
			@Value("${app.minio.bucket}") String bucket) {
		this.client = MinioClient.builder()
			.endpoint(endpoint)
			.credentials(accessKey, secretKey)
			.build();
		this.bucket = bucket;
	}

	@Override
	public void put(String objectKey, InputStream data, long size, String contentType) {
		try {
			client.putObject(PutObjectArgs.builder()
				.bucket(bucket).object(objectKey)
				.stream(data, size, -1)
				.contentType(contentType)
				.build());
		} catch (Exception e) {
			throw new IllegalStateException("파일 저장에 실패했습니다.", e);
		}
	}

	@Override
	public InputStream get(String objectKey) {
		try {
			return client.getObject(GetObjectArgs.builder()
				.bucket(bucket).object(objectKey)
				.build());
		} catch (Exception e) {
			throw new IllegalStateException("파일 조회에 실패했습니다.", e);
		}
	}

	@Override
	public void delete(String objectKey) {
		try {
			client.removeObject(RemoveObjectArgs.builder()
				.bucket(bucket).object(objectKey)
				.build());
		} catch (Exception e) {
			throw new IllegalStateException("파일 삭제에 실패했습니다.", e);
		}
	}
}
