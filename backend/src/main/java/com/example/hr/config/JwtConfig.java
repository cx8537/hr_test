package com.example.hr.config;

import com.example.hr.auth.jwt.JwtTokenProvider;
import java.time.Clock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** JWT 토큰 발급기 빈 구성. KST Clock 주입(FND-003/004). */
@Configuration
public class JwtConfig {

	@Bean
	JwtTokenProvider jwtTokenProvider(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-expires-in}") long accessExpiresMs,
			@Value("${app.jwt.refresh-expires-in}") long refreshExpiresMs,
			Clock clock) {
		return new JwtTokenProvider(secret, accessExpiresMs, refreshExpiresMs, clock);
	}
}
