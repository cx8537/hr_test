package com.example.hr.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 개발 단계 보안·CORS 설정.
 * 주의: 현재는 골격 단계로 모든 요청을 허용한다.
 * JWT 인증 필터와 인가(authenticated) 규칙은 Phase 1(FND-003~005, 010)에서 도입하며,
 * 그때 anyRequest().permitAll() 을 실제 RBAC 규칙으로 교체한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// 개발 CORS 허용 오리진(프론트 3000). 환경변수로 외부화.
	@Value("${app.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll() // TODO(Phase 1): JWT 인증 + RBAC 로 교체
			);
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(allowedOrigins);
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
}
