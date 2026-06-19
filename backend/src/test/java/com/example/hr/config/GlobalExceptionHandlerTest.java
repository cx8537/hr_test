package com.example.hr.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

/** 예외 → HTTP 상태 매핑 검증. */
class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void FND010_권한거부_403() {
		var response = handler.handleAccessDenied(new AccessDeniedException("권한 없음"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getBody()).containsEntry("error", "FORBIDDEN");
	}

	@Test
	void FND004_인증실패_401() {
		var response = handler.handleAuth(new BadCredentialsException("자격 증명 오류"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).containsEntry("error", "UNAUTHORIZED");
	}
}
