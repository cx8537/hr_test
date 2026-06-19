package com.example.hr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** JPA Auditing 활성화(BaseEntity의 created_at/updated_at 자동 설정). */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
