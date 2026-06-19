package com.example.hr.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** RSV-003: 예약 시간대 겹침 판정(맞닿음 허용, 포함·부분겹침 충돌). */
class TimeRangeOverlapTest {

	private static OffsetDateTime t(int hour) {
		return OffsetDateTime.of(2026, 6, 19, hour, 0, 0, 0, ZoneOffset.ofHours(9)); // KST
	}

	@Test
	void 완전_분리되면_겹치지_않음() {
		assertThat(TimeRangeOverlap.overlaps(t(9), t(10), t(11), t(12))).isFalse();
	}

	@Test
	void AC2_맞닿음_뒤쪽_허용() {
		// 9~10, 10~11: 끝=다음 시작 → 겹침 아님
		assertThat(TimeRangeOverlap.overlaps(t(9), t(10), t(10), t(11))).isFalse();
	}

	@Test
	void AC2_맞닿음_앞쪽_허용() {
		assertThat(TimeRangeOverlap.overlaps(t(10), t(11), t(9), t(10))).isFalse();
	}

	@Test
	void AC1_부분겹침_충돌() {
		assertThat(TimeRangeOverlap.overlaps(t(9), t(11), t(10), t(12))).isTrue();
	}

	@Test
	void AC1_완전포함_충돌() {
		assertThat(TimeRangeOverlap.overlaps(t(9), t(12), t(10), t(11))).isTrue();
	}

	@Test
	void AC1_동일구간_충돌() {
		assertThat(TimeRangeOverlap.overlaps(t(9), t(11), t(9), t(11))).isTrue();
	}

	@Test
	void 역전_구간_거부() {
		assertThatThrownBy(() -> TimeRangeOverlap.overlaps(t(11), t(9), t(12), t(13)))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void 길이0_구간_거부() {
		assertThatThrownBy(() -> TimeRangeOverlap.overlaps(t(9), t(9), t(10), t(11)))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
