package com.example.hr.reservation.service;

import com.example.hr.reservation.domain.ReservationStatus;
import com.example.hr.reservation.domain.TimeRangeOverlap;
import com.example.hr.reservation.entity.Reservation;
import com.example.hr.reservation.repository.ReservationRepository;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약 관리(RSV-002/003/005). 즉시 확정(승인 없음). 시간 겹침은 앱단 {@link TimeRangeOverlap} 사전검사 +
 * DB EXCLUSION 제약이 최종 보장(동시성 AC3). 취소는 본인 자유, 관리자 취소는 사유 필수(RSV-005).
 */
@Service
public class ReservationService {

	private final ReservationRepository reservationRepository;

	public ReservationService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	/** 즉시 예약(RSV-002/003). 기존 활성 예약과 겹치면 거부(AC1). 맞닿음은 허용(AC2). */
	@Transactional
	public Reservation reserve(Long resourceId, Long reserverId, OffsetDateTime startAt,
			OffsetDateTime endAt, String purpose, int headcount, String note,
			String destination, String driver) {
		boolean conflict = reservationRepository
			.findByResourceIdAndStatus(resourceId, ReservationStatus.ACTIVE).stream()
			.anyMatch(r -> TimeRangeOverlap.overlaps(startAt, endAt, r.getStartAt(), r.getEndAt()));
		if (conflict) {
			throw new IllegalStateException("해당 시간대에 이미 예약이 있습니다."); // RSV-003 AC1
		}
		Reservation reservation = new Reservation(resourceId, reserverId, startAt, endAt, purpose,
			headcount, note);
		if (destination != null || driver != null) {
			reservation.setVehicleInfo(destination, driver);
		}
		return reservationRepository.save(reservation);
	}

	/** 예약자 ID(취소 권한 판정용 — 본인 여부 확인). */
	@Transactional(readOnly = true)
	public Long reserverIdOf(Long reservationId) {
		return reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."))
			.getReserverId();
	}

	/**
	 * 취소(RSV-005). 본인은 자유 취소(AC1), 본인이 아니면 관리자 취소로 보고 사유 필수(AC2).
	 * 관리자 권한 자체는 컨트롤러 RBAC가, 알림(AC3)은 NOTI 모듈(Phase 9)이 처리한다.
	 */
	@Transactional
	public Reservation cancel(Long reservationId, Long requesterId, String reason) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
		if (reservation.getStatus() != ReservationStatus.ACTIVE) {
			throw new IllegalStateException("이미 취소된 예약입니다.");
		}
		boolean self = reservation.getReserverId().equals(requesterId);
		if (!self && (reason == null || reason.isBlank())) {
			throw new IllegalArgumentException("타인 예약 취소에는 사유가 필요합니다."); // RSV-005 AC2
		}
		reservation.cancel(requesterId, reason);
		return reservation;
	}
}
