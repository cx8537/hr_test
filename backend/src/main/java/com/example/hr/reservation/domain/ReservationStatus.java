package com.example.hr.reservation.domain;

/** 예약 상태(RSV-002/005). ACTIVE=확정(즉시 예약, 승인 없음), CANCELLED=취소. */
public enum ReservationStatus {
	ACTIVE,
	CANCELLED
}
