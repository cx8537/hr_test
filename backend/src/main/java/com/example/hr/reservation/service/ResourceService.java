package com.example.hr.reservation.service;

import com.example.hr.reservation.domain.ReservationStatus;
import com.example.hr.reservation.domain.ResourceType;
import com.example.hr.reservation.entity.Reservation;
import com.example.hr.reservation.entity.Resource;
import com.example.hr.reservation.repository.ReservationRepository;
import com.example.hr.reservation.repository.ResourceRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 자원 관리(RSV-001/007). 자원 등록·비활성화. 비활성화 시 미래 활성 예약을 자동 취소한다(RSV-007/LIFE-C3).
 * 취소 알림(AC2)은 NOTI 모듈(Phase 9)에서 도메인 이벤트로 처리한다.
 */
@Service
public class ResourceService {

	private final ResourceRepository resourceRepository;
	private final ReservationRepository reservationRepository;
	private final Clock clock;

	public ResourceService(ResourceRepository resourceRepository,
			ReservationRepository reservationRepository, Clock clock) {
		this.resourceRepository = resourceRepository;
		this.reservationRepository = reservationRepository;
		this.clock = clock;
	}

	@Transactional
	public Resource register(Long locationId, ResourceType type, String name) {
		return resourceRepository.save(new Resource(locationId, type, name));
	}

	/** 자원 비활성화(RSV-007): 미래 활성 예약을 모두 취소한다. 과거 예약은 보존(AC3). */
	@Transactional
	public int deactivate(Long resourceId, Long actorId) {
		Resource resource = resourceRepository.findById(resourceId)
			.orElseThrow(() -> new IllegalArgumentException("자원을 찾을 수 없습니다."));
		resource.deactivate();
		List<Reservation> future = reservationRepository
			.findByResourceIdAndStatusAndEndAtAfter(resourceId, ReservationStatus.ACTIVE,
				OffsetDateTime.now(clock));
		for (Reservation reservation : future) {
			reservation.cancel(actorId, "자원 비활성화로 자동 취소"); // RSV-007 AC1 (알림은 Phase 9)
		}
		return future.size();
	}

	@Transactional(readOnly = true)
	public Long locationIdOfResource(Long resourceId) {
		return resourceRepository.findById(resourceId)
			.orElseThrow(() -> new IllegalArgumentException("자원을 찾을 수 없습니다."))
			.getLocationId();
	}

	/** 예약이 속한 자원의 거점 ID(타인 예약 취소 RBAC 범위 판정용). */
	@Transactional(readOnly = true)
	public Long locationIdOfReservation(Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
		return locationIdOfResource(reservation.getResourceId());
	}
}
