package com.example.hr.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.reservation.domain.ReservationStatus;
import com.example.hr.reservation.domain.ResourceType;
import com.example.hr.reservation.entity.Reservation;
import com.example.hr.reservation.entity.Resource;
import com.example.hr.reservation.repository.ReservationRepository;
import com.example.hr.reservation.repository.ResourceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** RSV-001/007: 자원 등록·비활성화 시 미래 예약 자동 취소. */
class ResourceServiceTest {

	private ResourceRepository resourceRepository;
	private ReservationRepository reservationRepository;
	private com.example.hr.notification.service.NotificationService notificationService;
	private ResourceService resourceService;

	@BeforeEach
	void setUp() {
		resourceRepository = mock(ResourceRepository.class);
		reservationRepository = mock(ReservationRepository.class);
		notificationService = mock(com.example.hr.notification.service.NotificationService.class);
		Clock clock = Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC);
		resourceService = new ResourceService(resourceRepository, reservationRepository,
			notificationService, clock);
	}

	private static OffsetDateTime t(int day, int hour) {
		return OffsetDateTime.of(2026, 6, day, hour, 0, 0, 0, ZoneOffset.ofHours(9));
	}

	@Test
	void RSV001_자원_등록() {
		when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

		Resource r = resourceService.register(5L, ResourceType.MEETING_ROOM, "대회의실");

		assertThat(r.getName()).isEqualTo("대회의실");
		assertThat(r.getStatus()).isEqualTo(EntityStatus.ACTIVE);
	}

	@Test
	void RSV007_AC1_비활성화시_미래예약_자동취소() {
		Resource resource = new Resource(5L, ResourceType.VEHICLE, "차량1");
		when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
		Reservation future1 = new Reservation(1L, 100L, t(25, 9), t(25, 10), "출장", 1, null);
		Reservation future2 = new Reservation(1L, 101L, t(26, 9), t(26, 10), "출장", 1, null);
		when(reservationRepository.findByResourceIdAndStatusAndEndAtAfter(
			eq(1L), eq(ReservationStatus.ACTIVE), any())).thenReturn(List.of(future1, future2));

		int cancelled = resourceService.deactivate(1L, 999L);

		assertThat(cancelled).isEqualTo(2);
		assertThat(resource.getStatus()).isEqualTo(EntityStatus.INACTIVE);
		assertThat(future1.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
		assertThat(future2.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
		verify(notificationService, times(2)).create(any()); // 예약자별 자동취소 알림(RSV-007 AC2)
	}
}
