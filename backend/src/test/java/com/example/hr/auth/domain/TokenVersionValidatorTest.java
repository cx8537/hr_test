package com.example.hr.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** FND-004: 매 요청 계정 활성 상태 + 토큰 버전 검증(즉시 차단). */
class TokenVersionValidatorTest {

	@Test
	void FND004_AC3_활성_버전일치_유효() {
		assertThat(TokenVersionValidator.isValid(3, 3, true)).isTrue();
	}

	@Test
	void FND004_AC4_버전불일치_무효() {
		// 로그아웃·강제만료로 서버 token_version 증가 → 기존 Access 토큰 무효
		assertThat(TokenVersionValidator.isValid(3, 4, true)).isFalse();
	}

	@Test
	void FND004_AC3_비활성계정_무효() {
		assertThat(TokenVersionValidator.isValid(3, 3, false)).isFalse();
	}
}
