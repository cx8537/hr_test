package com.example.hr.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** KST 고정 Clock(시간 의존 로직 주입용, 테스트는 고정 Clock으로 대체). */
@Configuration
public class ClockConfig {

	@Bean
	Clock clock() {
		return Clock.system(ZoneId.of("Asia/Seoul"));
	}
}
