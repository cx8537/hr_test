package com.example.hr.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 인증 요청·응답 DTO(FND-003). */
public final class AuthDtos {

	private AuthDtos() {
	}

	public record LoginRequest(@NotBlank String loginId, @NotBlank String password) {
	}

	public record LoginResponse(String accessToken, String refreshToken, boolean mustChangePassword) {
	}

	public record RefreshRequest(@NotBlank String refreshToken) {
	}

	public record TokenResponse(String accessToken) {
	}
}
