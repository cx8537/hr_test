package com.example.hr.auth;

import com.example.hr.auth.domain.TokenVersionValidator;
import com.example.hr.auth.jwt.JwtTokenProvider;
import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.EmployeeRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 토큰 검증 필터(FND-004). Authorization Bearer 토큰을 검증하고,
 * 매 요청 계정 활성 상태 + 토큰 버전을 DB 조회로 확인한다(비활성·퇴사·로그아웃·강제만료 즉시 차단).
 * 역할은 토큰에 담지 않으므로 권한은 비우고, 인가(RBAC)는 별도 판정한다(FND-006/010).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final EmployeeRepository employeeRepository;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
			EmployeeRepository employeeRepository) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.employeeRepository = employeeRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(BEARER_PREFIX.length());
		try {
			Claims claims = jwtTokenProvider.parse(token).getPayload();
			Long employeeId = Long.valueOf(claims.getSubject());
			int tokenVersion = claims.get("tv", Integer.class);

			Optional<Employee> found = employeeRepository.findById(employeeId);
			boolean active = found.map(e -> e.getStatus() == EntityStatus.ACTIVE).orElse(false);
			int currentVersion = found.map(Employee::getTokenVersion).orElse(-1);

			if (!TokenVersionValidator.isValid(tokenVersion, currentVersion, active)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			var authentication = new UsernamePasswordAuthenticationToken(employeeId, null, List.of());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (JwtException | IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		filterChain.doFilter(request, response);
	}
}
