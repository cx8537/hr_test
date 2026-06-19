package com.example.hr.approval.domain;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * 휴가 일수 계산기(AP-042). 영업일 기준(토·일 제외 AC1), 공휴일 제외(AC3),
 * 반차=0.5일이며 단일 날짜에만 허용(AC2), 종료일<시작일 거부(AC4).
 * 미등록 공휴일은 영업일로 간주. 공휴일 집합을 주입받아 결정론적으로 계산. 순수 함수.
 */
public final class LeaveDaysCalculator {

	private static final BigDecimal HALF_DAY = new BigDecimal("0.5");

	private LeaveDaysCalculator() {
	}

	public static BigDecimal calculate(LocalDate start, LocalDate end, boolean halfDay,
			Set<LocalDate> holidays) {
		if (end.isBefore(start)) {
			throw new IllegalArgumentException("종료일이 시작일보다 빠를 수 없습니다."); // AC4
		}
		if (halfDay) {
			if (!start.equals(end)) {
				throw new IllegalArgumentException("반차는 단일 날짜에만 허용됩니다."); // AC2
			}
			return isBusinessDay(start, holidays) ? HALF_DAY : BigDecimal.ZERO;
		}

		long businessDays = 0;
		for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
			if (isBusinessDay(date, holidays)) {
				businessDays++;
			}
		}
		return BigDecimal.valueOf(businessDays);
	}

	private static boolean isBusinessDay(LocalDate date, Set<LocalDate> holidays) {
		DayOfWeek dow = date.getDayOfWeek();
		if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
			return false;
		}
		return !holidays.contains(date);
	}
}
