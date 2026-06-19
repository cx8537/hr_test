package com.example.hr.auth;

import com.example.hr.auth.dto.AuthDtos.LoginRequest;
import com.example.hr.auth.dto.AuthDtos.LoginResponse;
import com.example.hr.auth.dto.AuthDtos.RefreshRequest;
import com.example.hr.auth.dto.AuthDtos.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 API(FND-003): 로그인 / 토큰 갱신. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request.loginId(), request.password());
	}

	@PostMapping("/refresh")
	public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request.refreshToken());
	}
}
