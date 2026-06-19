package com.example.hr.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * MinIO 파일 저장(AP-040)을 실 MinIO에 대해 검증한다.
 * local 프로파일에서 app.minio.enabled=true 이므로 MinioFileStorage 빈이 주입된다.
 * 업로드한 바이트를 다시 받아 동일성 확인 후 삭제, 삭제 후 조회 실패까지 검증한다.
 */
@SpringBootTest
@ActiveProfiles("local")
class MinioFileStorageIT {

	@Autowired
	private FileStorage fileStorage;

	@Test
	void 업로드_다운로드_삭제_왕복() throws Exception {
		String key = "it/minio-roundtrip-" + System.nanoTime() + ".txt";
		byte[] content = "안녕하세요 MinIO 통합테스트".getBytes(StandardCharsets.UTF_8);

		fileStorage.put(key, new java.io.ByteArrayInputStream(content),
			content.length, "text/plain");

		byte[] downloaded;
		try (InputStream in = fileStorage.get(key)) {
			downloaded = in.readAllBytes();
		}
		assertThat(downloaded).isEqualTo(content);

		fileStorage.delete(key);
		assertThatThrownBy(() -> fileStorage.get(key).close())
			.isInstanceOf(IllegalStateException.class);
	}
}
