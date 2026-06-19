package com.example.hr.auth;

import com.example.hr.auth.dto.AuthDtos.LoginResponse;
import com.example.hr.auth.dto.AuthDtos.TokenResponse;
import com.example.hr.auth.entity.RefreshToken;
import com.example.hr.auth.jwt.JwtTokenProvider;
import com.example.hr.auth.repository.RefreshTokenRepository;
import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.EmployeeRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스(FND-003). 로그인 시 비밀번호(BCrypt) 검증 후 Access+Refresh 발급,
 * 비활성 계정은 거부(AC2). 토큰 갱신은 Refresh 검증 후 새 Access 발급.
 */
@Service
public class AuthService {

	private final EmployeeRepository employeeRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final Clock clock;
	private final long refreshExpiresMs;

	public AuthService(EmployeeRepository employeeRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenProvider jwtTokenProvider,
			Clock clock,
			@Value("${app.jwt.refresh-expires-in}") long refreshExpiresMs) {
		this.employeeRepository = employeeRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.clock = clock;
		this.refreshExpiresMs = refreshExpiresMs;
	}

	@Transactional
	public LoginResponse login(String loginId, String rawPassword) {
		Employee employee = employeeRepository.findByLoginId(loginId)
			.orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));

		if (employee.getStatus() != EntityStatus.ACTIVE) {
			throw new DisabledException("비활성 계정입니다."); // FND-003 AC2
		}
		if (!passwordEncoder.matches(rawPassword, employee.getPasswordHash())) {
			throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
		}

		String subject = employee.getId().toString();
		String accessToken = jwtTokenProvider.createAccessToken(subject, employee.getTokenVersion());
		String refreshToken = jwtTokenProvider.createRefreshToken(subject);

		OffsetDateTime now = OffsetDateTime.now(clock);
		refreshTokenRepository.save(new RefreshToken(
			employee.getId(),
			passwordEncoder.encode(refreshToken),
			now,
			now.plusNanos(refreshExpiresMs * 1_000_000)));

		return new LoginResponse(accessToken, refreshToken, employee.isMustChangePassword());
	}

	@Transactional(readOnly = true)
	public TokenResponse refresh(String refreshToken) {
		// 서명·만료 검증(실패 시 JwtException). 상세 폐기·해시 대조는 FND-004(1-4)에서 강화.
		String subject = jwtTokenProvider.getSubject(refreshToken);
		Employee employee = employeeRepository.findById(Long.valueOf(subject))
			.orElseThrow(() -> new BadCredentialsException("유효하지 않은 토큰입니다."));
		if (employee.getStatus() != EntityStatus.ACTIVE) {
			throw new DisabledException("비활성 계정입니다.");
		}
		String accessToken = jwtTokenProvider.createAccessToken(subject, employee.getTokenVersion());
		return new TokenResponse(accessToken);
	}
}
