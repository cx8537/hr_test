package com.example.hr.location.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.hr.location.domain.LocationDeactivation.Decision;
import org.junit.jupiter.api.Test;

/** LOC-006/LIFE-C1: 거점 비활성화 가능 판정(자원 잔존 시 거부, 영향 건수 경고). */
class LocationDeactivationTest {

	@Test
	void 자원0_예약0이면_허용() {
		Decision d = LocationDeactivation.evaluate(0, 0);
		assertThat(d.allowed()).isTrue();
		assertThat(d.reason()).isNull();
	}

	@Test
	void LOC006_AC1_소속자원_남으면_거부() {
		Decision d = LocationDeactivation.evaluate(1, 0);
		assertThat(d.allowed()).isFalse();
		assertThat(d.reason()).contains("1건");
	}

	@Test
	void 경계값_자원_정확히0이면_허용() {
		assertThat(LocationDeactivation.evaluate(0, 5).allowed()).isTrue();
	}

	@Test
	void LIFEC1_AC2_허용시에도_미래예약_건수_노출() {
		Decision d = LocationDeactivation.evaluate(0, 5);
		assertThat(d.allowed()).isTrue();
		assertThat(d.futureReservationCount()).isEqualTo(5); // 사전 경고용 영향 건수
	}

	@Test
	void LIFEC1_AC2_거부시_영향건수_노출() {
		Decision d = LocationDeactivation.evaluate(3, 7);
		assertThat(d.allowed()).isFalse();
		assertThat(d.activeResourceCount()).isEqualTo(3);
		assertThat(d.futureReservationCount()).isEqualTo(7);
	}

	@Test
	void 음수_입력_거부() {
		assertThatThrownBy(() -> LocationDeactivation.evaluate(-1, 0))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> LocationDeactivation.evaluate(0, -1))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
