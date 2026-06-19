package com.example.hr.location.service;

/**
 * 거점 비활성화 사전 검사를 위한 자원/예약 건수 조회(LIFE-C1). 모듈 경계 분리용 인터페이스.
 * AST/RSV 모듈 도입 후 실제 집계 구현으로 교체한다(현재 기본 구현은 0건).
 */
public interface LocationResourceCounter {

	/** 거점 비활성화 영향 건수(활성 자원 수, 미래 예약 수). */
	record Counts(int activeResources, int futureReservations) {
	}

	Counts countFor(Long locationId);
}
