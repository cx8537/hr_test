package com.example.hr.auth.domain;

/**
 * 토큰 유효성 판정(FND-004): 매 보호 요청마다 계정 활성 상태와 토큰 버전을 확인한다.
 * 로그아웃·퇴사·강제만료 시 서버의 token_version을 올리므로, 토큰에 담긴 버전과
 * 현재 계정 버전이 다르면 즉시 무효(만료를 기다리지 않음).
 */
public final class TokenVersionValidator {

	private TokenVersionValidator() {
	}

	public static boolean isValid(int tokenVersionInToken, int currentAccountVersion, boolean accountActive) {
		return accountActive && tokenVersionInToken == currentAccountVersion;
	}
}
