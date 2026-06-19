package com.example.hr.reservation.repository;

import com.example.hr.reservation.domain.ReservationStatus;
import com.example.hr.reservation.entity.Reservation;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	/** 자원의 특정 상태 예약(겹침 사전 검사용). */
	List<Reservation> findByResourceIdAndStatus(Long resourceId, ReservationStatus status);

	/** 자원의 미래 활성 예약(자원 비활성화 시 자동 취소 대상 RSV-007). */
	List<Reservation> findByResourceIdAndStatusAndEndAtAfter(Long resourceId,
			ReservationStatus status, OffsetDateTime now);
}
