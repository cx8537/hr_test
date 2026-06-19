package com.example.hr.config;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인프라 연결 확인용 헬스 체크 엔드포인트.
 * DB 연결 가능 여부를 반환한다(0-3 연결 검증용). MinIO 연결 확인은 SDK 도입 단계에서 추가.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

	private final DataSource dataSource;

	public HealthController(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@GetMapping("/health")
	public Map<String, Object> health() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", "UP");
		body.put("db", pingDb() ? "UP" : "DOWN");
		return body;
	}

	private boolean pingDb() {
		try (Connection c = dataSource.getConnection()) {
			return c.isValid(2);
		} catch (Exception e) {
			return false;
		}
	}
}
