package com.example.hr.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** LIFE-A5: 시스템관리자 최소 1명 보장(self-lockout 방지). */
class SysAdminGuardTest {

	@Test
	void AC1_마지막_시스템관리자_제외_거부() {
		assertThat(SysAdminGuard.canRemove(Set.of(1L), 1L)).isFalse();
	}

	@Test
	void AC2_2명중_1명_제외_허용() {
		assertThat(SysAdminGuard.canRemove(Set.of(1L, 2L), 1L)).isTrue();
	}

	@Test
	void 대상이_시스템관리자_아니면_무관_허용() {
		assertThat(SysAdminGuard.canRemove(Set.of(1L, 2L), 99L)).isTrue();
		assertThat(SysAdminGuard.canRemove(Set.of(), 99L)).isTrue();
	}

	@Test
	void requireCanRemove_마지막_1명이면_예외() {
		assertThatThrownBy(() -> SysAdminGuard.requireCanRemove(Set.of(7L), 7L))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void requireCanRemove_2명이상이면_통과() {
		SysAdminGuard.requireCanRemove(Set.of(7L, 8L), 7L); // 예외 없음
	}
}
