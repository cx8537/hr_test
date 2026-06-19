package com.example.hr.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 * JWT 발급·검증(FND-003/004). jjwt 기반, java.time.Clock 주입으로 결정론적 테스트.
 * Access 토큰에는 사용자 식별자(subject)·만료·토큰버전(tv)만 담는다.
 * 역할 정보는 토큰에 넣지 않고 서버가 매 요청 조회한다(FND-003 AC4).
 */
public class JwtTokenProvider {

	private static final String CLAIM_TOKEN_VERSION = "tv";

	private final SecretKey key;
	private final long accessExpiresMs;
	private final long refreshExpiresMs;
	private final Clock clock;

	public JwtTokenProvider(String secret, long accessExpiresMs, long refreshExpiresMs, Clock clock) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessExpiresMs = accessExpiresMs;
		this.refreshExpiresMs = refreshExpiresMs;
		this.clock = clock;
	}

	public String createAccessToken(String subject, int tokenVersion) {
		Date now = Date.from(clock.instant());
		return Jwts.builder()
			.subject(subject)
			.claim(CLAIM_TOKEN_VERSION, tokenVersion)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + accessExpiresMs))
			.signWith(key)
			.compact();
	}

	public String createRefreshToken(String subject) {
		Date now = Date.from(clock.instant());
		return Jwts.builder()
			.subject(subject)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + refreshExpiresMs))
			.signWith(key)
			.compact();
	}

	/** 서명·만료를 검증하고 파싱한다. 실패 시 JwtException(만료=ExpiredJwtException). */
	public Jws<Claims> parse(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.clock(() -> Date.from(clock.instant()))
			.build()
			.parseSignedClaims(token);
	}

	public String getSubject(String token) {
		return parse(token).getPayload().getSubject();
	}

	public int getTokenVersion(String token) {
		return parse(token).getPayload().get(CLAIM_TOKEN_VERSION, Integer.class);
	}
}
