package com.example.hr.approval.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** AP-042: 휴가 일수 계산기(영업일·공휴일 제외, 반차 0.5일). */
class LeaveDaysCalculatorTest {

	// 2026-06-15(월) ~ 06-19(금) 평일, 06-20(토)/06-21(일) 주말
	private static final LocalDate MON = LocalDate.of(2026, 6, 15);
	private static final LocalDate WED = LocalDate.of(2026, 6, 17);
	private static final LocalDate FRI = LocalDate.of(2026, 6, 19);
	private static final LocalDate SAT = LocalDate.of(2026, 6, 20);
	private static final LocalDate SUN = LocalDate.of(2026, 6, 21);

	@Test
	void AP042_AC1_평일5일() {
		assertThat(LeaveDaysCalculator.calculate(MON, FRI, false, Set.of()))
			.isEqualByComparingTo("5");
	}

	@Test
	void AP042_AC1_주말제외() {
		// 월~일(7일)에서 토·일 제외 → 5
		assertThat(LeaveDaysCalculator.calculate(MON, SUN, false, Set.of()))
			.isEqualByComparingTo("5");
	}

	@Test
	void AP042_AC3_공휴일제외() {
		assertThat(LeaveDaysCalculator.calculate(MON, FRI, false, Set.of(WED)))
			.isEqualByComparingTo("4");
	}

	@Test
	void AP042_AC2_반차_단일평일_0_5일() {
		assertThat(LeaveDaysCalculator.calculate(FRI, FRI, true, Set.of()))
			.isEqualByComparingTo("0.5");
	}

	@Test
	void AP042_AC2_반차_복수날짜_거부() {
		assertThatThrownBy(() -> LeaveDaysCalculator.calculate(MON, FRI, true, Set.of()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP042_AC4_종료일이_시작일보다_빠르면_거부() {
		assertThatThrownBy(() -> LeaveDaysCalculator.calculate(FRI, MON, false, Set.of()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP042_단일_평일_종일_1일() {
		assertThat(LeaveDaysCalculator.calculate(FRI, FRI, false, Set.of()))
			.isEqualByComparingTo("1");
	}

	@Test
	void AP042_주말만_포함_0일() {
		assertThat(LeaveDaysCalculator.calculate(SAT, SUN, false, Set.of()))
			.isEqualByComparingTo("0");
	}
}
