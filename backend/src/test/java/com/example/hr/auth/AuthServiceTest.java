package com.example.hr.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.auth.dto.AuthDtos.LoginResponse;
import com.example.hr.auth.entity.RefreshToken;
import com.example.hr.auth.jwt.JwtTokenProvider;
import com.example.hr.auth.repository.RefreshTokenRepository;
import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.EmployeeRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

/** FND-003: 로그인 비즈니스 로직(Mockito, DB 불필요). */
class AuthServiceTest {

	private EmployeeRepository employeeRepository;
	private RefreshTokenRepository refreshTokenRepository;
	private PasswordEncoder passwordEncoder;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		employeeRepository = mock(EmployeeRepository.class);
		refreshTokenRepository = mock(RefreshTokenRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		JwtTokenProvider jwt = new JwtTokenProvider(
			"0123456789-0123456789-0123456789-AB", 900_000L, 1_209_600_000L,
			Clock.fixed(Instant.parse("2026-06-19T00:00:00Z"), ZoneOffset.UTC));
		authService = new AuthService(employeeRepository, refreshTokenRepository,
			passwordEncoder, jwt, Clock.fixed(Instant.parse("2026-06-19T00:00:00Z"), ZoneOffset.UTC),
			1_209_600_000L);
	}

	private Employee activeEmployee(boolean mustChange) {
		Employee emp = mock(Employee.class);
		lenient().when(emp.getId()).thenReturn(1L);
		lenient().when(emp.getStatus()).thenReturn(EntityStatus.ACTIVE);
		lenient().when(emp.getPasswordHash()).thenReturn("$2a$10$hash");
		lenient().when(emp.getTokenVersion()).thenReturn(0);
		lenient().when(emp.isMustChangePassword()).thenReturn(mustChange);
		return emp;
	}

	@Test
	void FND003_AC1_정상로그인_토큰발급() {
		Employee emp = activeEmployee(true);
		when(employeeRepository.findByLoginId("hong")).thenReturn(Optional.of(emp));
		when(passwordEncoder.matches("pw", "$2a$10$hash")).thenReturn(true);
		// refresh 토큰은 SHA-256으로 해시 저장(BCrypt encode 미사용)

		LoginResponse res = authService.login("hong", "pw");

		assertThat(res.accessToken()).isNotBlank();
		assertThat(res.refreshToken()).isNotBlank();
		assertThat(res.mustChangePassword()).isTrue();
		verify(refreshTokenRepository).save(any(RefreshToken.class));
	}

	@Test
	void FND003_AC2_비활성계정_로그인거부() {
		Employee inactive = mock(Employee.class);
		when(inactive.getStatus()).thenReturn(EntityStatus.INACTIVE);
		when(employeeRepository.findByLoginId("hong")).thenReturn(Optional.of(inactive));

		assertThatThrownBy(() -> authService.login("hong", "pw"))
			.isInstanceOf(DisabledException.class);
		verify(refreshTokenRepository, never()).save(any());
	}

	@Test
	void FND003_비밀번호불일치_거부() {
		Employee emp = activeEmployee(false);
		when(employeeRepository.findByLoginId("hong")).thenReturn(Optional.of(emp));
		when(passwordEncoder.matches("wrong", "$2a$10$hash")).thenReturn(false);

		assertThatThrownBy(() -> authService.login("hong", "wrong"))
			.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void FND003_존재하지않는_ID_거부() {
		when(employeeRepository.findByLoginId("none")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login("none", "pw"))
			.isInstanceOf(BadCredentialsException.class);
	}
}
