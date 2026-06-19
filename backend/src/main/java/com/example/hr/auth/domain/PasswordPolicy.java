package com.example.hr.auth.domain;

/**
 * 비밀번호 정책 검증(FND-003 AC3): 8자 이상, 영문+숫자 포함.
 * 특수문자·주기적 변경은 요구하지 않는다(01-foundation §2 주석).
 */
public final class PasswordPolicy {

	private static final int MIN_LENGTH = 8;

	private PasswordPolicy() {
	}

	public static boolean isValid(String password) {
		if (password == null || password.length() < MIN_LENGTH) {
			return false;
		}
		boolean hasLetter = password.chars().anyMatch(Character::isLetter);
		boolean hasDigit = password.chars().anyMatch(Character::isDigit);
		return hasLetter && hasDigit;
	}
}
