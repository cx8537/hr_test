package com.example.hr.reservation.domain;

import java.time.OffsetDateTime;

/**
 * 예약 시간대 겹침 판정(RSV-003). 순수 함수.
 * 구간은 반열림 [start, end) 으로 본다 → 경계가 맞닿는 예약(끝=다음 시작)은 겹침이 아니다(AC2).
 * 포함·부분겹침은 충돌(AC1). 종료 ≤ 시작은 잘못된 구간으로 거부.
 */
public final class TimeRangeOverlap {

	private TimeRangeOverlap() {
	}

	/** 두 구간이 겹치면 true. 맞닿음(aEnd==bStart 또는 bEnd==aStart)은 false. */
	public static boolean overlaps(OffsetDateTime aStart, OffsetDateTime aEnd,
			OffsetDateTime bStart, OffsetDateTime bEnd) {
		requireValid(aStart, aEnd);
		requireValid(bStart, bEnd);
		// [aStart,aEnd) 와 [bStart,bEnd) 가 겹치려면 aStart < bEnd 이고 bStart < aEnd.
		return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
	}

	private static void requireValid(OffsetDateTime start, OffsetDateTime end) {
		if (!start.isBefore(end)) {
			throw new IllegalArgumentException("종료 시각은 시작 시각보다 뒤여야 합니다.");
		}
	}
}
