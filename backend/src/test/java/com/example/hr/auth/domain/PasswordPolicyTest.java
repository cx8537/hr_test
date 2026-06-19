package com.example.hr.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** FND-003 AC3: 비밀번호는 8자 이상, 영문+숫자 포함. */
class PasswordPolicyTest {

	@Test
	void FND003_AC3_8자미만_거부() {
		assertThat(PasswordPolicy.isValid("abc123")).isFalse(); // 6자
		assertThat(PasswordPolicy.isValid("abcdef1")).isFalse(); // 7자
	}

	@Test
	void FND003_AC3_경계값_8자_허용() {
		assertThat(PasswordPolicy.isValid("abcd1234")).isTrue(); // 8자
	}

	@Test
	void FND003_AC3_영문없음_거부() {
		assertThat(PasswordPolicy.isValid("12345678")).isFalse();
	}

	@Test
	void FND003_AC3_숫자없음_거부() {
		assertThat(PasswordPolicy.isValid("abcdefgh")).isFalse();
	}

	@Test
	void FND003_AC3_null_거부() {
		assertThat(PasswordPolicy.isValid(null)).isFalse();
	}
}
