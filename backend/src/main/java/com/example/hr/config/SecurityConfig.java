package com.example.hr.config;

import com.example.hr.auth.JwtAuthenticationFilter;
import com.example.hr.auth.jwt.JwtTokenProvider;
import com.example.hr.org.repository.EmployeeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 보안·CORS 설정(FND-004/010). 공개 엔드포인트(로그인·토큰갱신·헬스)를 제외한 모든 요청은
 * 인증을 요구한다. JWT 토큰 검증은 무상태(STATELESS) 필터로 매 요청 수행한다.
 * 역할 기반 세부 인가(RBAC)는 도메인 RbacPolicy + 메서드/엔드포인트 보안으로 후속 단계에서 강제한다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// 개발 CORS 허용 오리진(프론트 3000). 환경변수로 외부화.
	@Value("${app.cors.allowed-origins}")
	private List<String> allowedOrigins;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider,
			EmployeeRepository employeeRepository) throws Exception {
		JwtAuthenticationFilter jwtFilter =
			new JwtAuthenticationFilter(jwtTokenProvider, employeeRepository);
		http
			.csrf(csrf -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/health").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	// 비밀번호 단방향 해시(FND-003: BCrypt). 평문 저장 금지.
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
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
