package com.example.hr.config;

import io.jsonwebtoken.JwtException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 예외 → HTTP 상태 매핑(FND-004/010 등). 권한 거부 403, 인증 실패 401, 검증 실패 400. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
		return body(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage());
	}

	@ExceptionHandler({AuthenticationException.class, JwtException.class})
	public ResponseEntity<Map<String, String>> handleAuth(Exception e) {
		return body(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
		return body(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청 값이 올바르지 않습니다.");
	}

	private ResponseEntity<Map<String, String>> body(HttpStatus status, String error, String message) {
		return ResponseEntity.status(status)
			.body(Map.of("error", error, "message", message == null ? "" : message));
	}
}
