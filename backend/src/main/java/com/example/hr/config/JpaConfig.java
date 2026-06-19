package com.example.hr.config;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화(BaseEntity의 created_at/updated_at 자동 설정).
 * BaseEntity의 감사 필드가 OffsetDateTime이라, 기본 제공자(LocalDateTime)로는 변환 실패한다.
 * KST Clock 기반 OffsetDateTime DateTimeProvider를 지정해 시각을 KST로 고정한다.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {

	@Bean
	DateTimeProvider auditingDateTimeProvider(Clock clock) {
		return () -> Optional.of(OffsetDateTime.now(clock));
	}
}
