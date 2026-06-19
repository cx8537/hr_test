package com.example.hr.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** FND-003/004: JWT 발급·검증 순수 로직(jjwt + Clock 주입, 결정론적). */
class JwtTokenProviderTest {

	private static final String SECRET = "0123456789-0123456789-0123456789-AB"; // >= 32 bytes
	private static final long ACCESS_MS = 900_000L;
	private static final long REFRESH_MS = 1_209_600_000L;
	private static final Instant T0 = Instant.parse("2026-06-19T00:00:00Z");

	private static Clock fixed(Instant i) {
		return Clock.fixed(i, ZoneOffset.UTC);
	}

	private static JwtTokenProvider provider(Clock clock) {
		return new JwtTokenProvider(SECRET, ACCESS_MS, REFRESH_MS, clock);
	}

	@Test
	void FND003_액세스토큰_subject_추출() {
		JwtTokenProvider p = provider(fixed(T0));
		String token = p.createAccessToken("42", 0);
		assertThat(p.getSubject(token)).isEqualTo("42");
	}

	@Test
	void FND003_AC4_액세스토큰에_역할클레임_없음() {
		JwtTokenProvider p = provider(fixed(T0));
		Claims claims = p.parse(p.createAccessToken("42", 3)).getPayload();
		assertThat(claims.get("roles")).isNull();
		assertThat(claims.get("role")).isNull();
		assertThat(claims.getSubject()).isEqualTo("42");
	}

	@Test
	void FND004_토큰버전_클레임_보존() {
		JwtTokenProvider p = provider(fixed(T0));
		String token = p.createAccessToken("42", 7);
		assertThat(p.getTokenVersion(token)).isEqualTo(7);
	}

	@Test
	void FND004_AC1_만료된_토큰_검증실패() {
		JwtTokenProvider issuer = new JwtTokenProvider(SECRET, 1_000L, 1_000L, fixed(T0));
		JwtTokenProvider verifier = new JwtTokenProvider(SECRET, 1_000L, 1_000L, fixed(T0.plusSeconds(10)));
		String token = issuer.createAccessToken("1", 0);
		assertThatThrownBy(() -> verifier.parse(token)).isInstanceOf(ExpiredJwtException.class);
	}

	@Test
	void FND004_AC2_위조서명_검증실패() {
		JwtTokenProvider issuer = provider(fixed(T0));
		JwtTokenProvider other = new JwtTokenProvider(
			"ZZZZZZZZZZ-ZZZZZZZZZZ-ZZZZZZZZZZ-YY", ACCESS_MS, REFRESH_MS, fixed(T0));
		String token = issuer.createAccessToken("1", 0);
		assertThatThrownBy(() -> other.parse(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void FND003_리프레시토큰_subject_추출() {
		JwtTokenProvider p = provider(fixed(T0));
		String token = p.createRefreshToken("42");
		assertThat(p.getSubject(token)).isEqualTo("42");
	}
}
