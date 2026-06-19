package com.example.hr.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.auth.jwt.JwtTokenProvider;
import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.EmployeeRepository;
import jakarta.servlet.FilterChain;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

/** FND-004: 토큰 검증 필터(매 요청 계정상태+토큰버전 확인, 즉시 차단). */
class JwtAuthenticationFilterTest {

	private static final String SECRET = "0123456789-0123456789-0123456789-AB";
	private static final Instant T0 = Instant.parse("2026-06-19T00:00:00Z");

	private JwtTokenProvider jwt;
	private EmployeeRepository employeeRepository;
	private JwtAuthenticationFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private FilterChain chain;

	@BeforeEach
	void setUp() {
		jwt = new JwtTokenProvider(SECRET, 900_000L, 1_209_600_000L,
			Clock.fixed(T0, ZoneOffset.UTC));
		employeeRepository = mock(EmployeeRepository.class);
		filter = new JwtAuthenticationFilter(jwt, employeeRepository);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		chain = mock(FilterChain.class);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private Employee employee(EntityStatus status, int tokenVersion) {
		Employee emp = mock(Employee.class);
		when(emp.getStatus()).thenReturn(status);
		when(emp.getTokenVersion()).thenReturn(tokenVersion);
		return emp;
	}

	@Test
	void 유효토큰_인증설정_통과() throws Exception {
		Employee emp = employee(EntityStatus.ACTIVE, 0);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
		request.addHeader("Authorization", "Bearer " + jwt.createAccessToken("1", 0));

		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(1L);
		verify(chain).doFilter(request, response);
		assertThat(response.getStatus()).isEqualTo(200);
	}

	@Test
	void 토큰없음_인증없이_통과() throws Exception {
		filter.doFilter(request, response, chain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(chain).doFilter(request, response);
	}

	@Test
	void FND004_AC1_만료토큰_401_차단() throws Exception {
		JwtTokenProvider pastIssuer = new JwtTokenProvider(SECRET, 1_000L, 1_000L,
			Clock.fixed(T0.minusSeconds(10), ZoneOffset.UTC));
		request.addHeader("Authorization", "Bearer " + pastIssuer.createAccessToken("1", 0));

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		verify(chain, never()).doFilter(request, response);
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void FND004_AC2_위조서명_401_차단() throws Exception {
		JwtTokenProvider forged = new JwtTokenProvider(
			"ZZZZZZZZZZ-ZZZZZZZZZZ-ZZZZZZZZZZ-YY", 900_000L, 1_209_600_000L,
			Clock.fixed(T0, ZoneOffset.UTC));
		request.addHeader("Authorization", "Bearer " + forged.createAccessToken("1", 0));

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		verify(chain, never()).doFilter(request, response);
	}

	@Test
	void FND004_토큰버전_불일치_401_차단() throws Exception {
		// 서버 token_version=5, 토큰의 tv=0 → 로그아웃/강제만료된 토큰
		Employee emp = employee(EntityStatus.ACTIVE, 5);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
		request.addHeader("Authorization", "Bearer " + jwt.createAccessToken("1", 0));

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		verify(chain, never()).doFilter(request, response);
	}

	@Test
	void FND004_AC3_비활성계정_401_차단() throws Exception {
		Employee emp = employee(EntityStatus.INACTIVE, 0);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
		request.addHeader("Authorization", "Bearer " + jwt.createAccessToken("1", 0));

		filter.doFilter(request, response, chain);

		assertThat(response.getStatus()).isEqualTo(401);
		verify(chain, never()).doFilter(request, response);
	}
}
