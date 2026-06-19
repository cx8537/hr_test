package com.example.hr.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.reservation.domain.ReservationStatus;
import com.example.hr.reservation.entity.Reservation;
import com.example.hr.reservation.repository.ReservationRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RSV-002/003/005: 즉시 예약·겹침 거부·맞닿음 허용·취소. */
class ReservationServiceTest {

	private ReservationRepository reservationRepository;
	private ReservationService reservationService;

	@BeforeEach
	void setUp() {
		reservationRepository = mock(ReservationRepository.class);
		reservationService = new ReservationService(reservationRepository);
	}

	private static OffsetDateTime t(int hour) {
		return OffsetDateTime.of(2026, 6, 20, hour, 0, 0, 0, ZoneOffset.ofHours(9));
	}

	private Reservation active(int startHour, int endHour) {
		return new Reservation(1L, 100L, t(startHour), t(endHour), "회의", 3, null);
	}

	@Test
	void RSV002_즉시예약_확정() {
		when(reservationRepository.findByResourceIdAndStatus(1L, ReservationStatus.ACTIVE))
			.thenReturn(List.of());
		when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

		Reservation r = reservationService.reserve(1L, 100L, t(9), t(10), "회의", 3, null, null, null);

		assertThat(r.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
	}

	@Test
	void RSV003_AC1_겹치면_거부() {
		when(reservationRepository.findByResourceIdAndStatus(1L, ReservationStatus.ACTIVE))
			.thenReturn(List.of(active(9, 11)));

		assertThatThrownBy(() ->
			reservationService.reserve(1L, 200L, t(10), t(12), "회의", 2, null, null, null))
			.isInstanceOf(IllegalStateException.class);
		verify(reservationRepository, never()).save(any());
	}

	@Test
	void RSV003_AC2_맞닿음_허용() {
		when(reservationRepository.findByResourceIdAndStatus(1L, ReservationStatus.ACTIVE))
			.thenReturn(List.of(active(9, 10)));
		when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

		Reservation r = reservationService.reserve(1L, 200L, t(10), t(11), "회의", 2, null, null, null);

		assertThat(r.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
	}

	@Test
	void RSV004_차량_행선지_운전자_저장() {
		when(reservationRepository.findByResourceIdAndStatus(1L, ReservationStatus.ACTIVE))
			.thenReturn(List.of());
		when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

		Reservation r = reservationService.reserve(1L, 100L, t(9), t(10), "출장", 1, null, "부산", "홍길동");

		assertThat(r.getDestination()).isEqualTo("부산");
		assertThat(r.getDriver()).isEqualTo("홍길동");
	}

	@Test
	void RSV005_AC1_본인_취소() {
		Reservation r = active(9, 10);
		when(reservationRepository.findById(5L)).thenReturn(Optional.of(r));

		reservationService.cancel(5L, 100L, null); // 본인(100L), 사유 없이 가능

		assertThat(r.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
	}

	@Test
	void RSV005_AC2_관리자_취소_사유필수() {
		Reservation r = active(9, 10);
		when(reservationRepository.findById(5L)).thenReturn(Optional.of(r));

		assertThatThrownBy(() -> reservationService.cancel(5L, 999L, null)) // 타인, 사유 없음
			.isInstanceOf(IllegalArgumentException.class);
		assertThat(r.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
	}

	@Test
	void RSV005_관리자_취소_사유포함_허용() {
		Reservation r = active(9, 10);
		when(reservationRepository.findById(5L)).thenReturn(Optional.of(r));

		reservationService.cancel(5L, 999L, "긴급 점검");

		assertThat(r.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
		assertThat(r.getCancelReason()).isEqualTo("긴급 점검");
		assertThat(r.getCancelledById()).isEqualTo(999L);
	}
}
