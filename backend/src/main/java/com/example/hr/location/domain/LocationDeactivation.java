package com.example.hr.location.domain;

/**
 * 거점 비활성화 가능 판정(LOC-006/LIFE-C1). 순수 함수.
 * 소속 활성 자원이 남아 있으면 비활성화를 거부하며(AC1), 비활성화 전 영향 건수
 * (활성 자원 수·미래 예약 수)를 함께 산출해 사전 경고로 노출한다(LIFE-C1 AC2).
 * 거점은 물리 삭제하지 않고 비활성화만 한다(CLAUDE.md 삭제 대신 비활성화).
 */
public final class LocationDeactivation {

	/** 판정 결과: 허용 여부 + 영향 건수(경고) + 거부 사유(허용 시 null). */
	public record Decision(boolean allowed, int activeResourceCount, int futureReservationCount,
			String reason) {
	}

	private LocationDeactivation() {
	}

	public static Decision evaluate(int activeResourceCount, int futureReservationCount) {
		if (activeResourceCount < 0 || futureReservationCount < 0) {
			throw new IllegalArgumentException("건수는 음수일 수 없습니다.");
		}
		if (activeResourceCount > 0) {
			String reason = "소속 자원 " + activeResourceCount
				+ "건이 남아 있어 거점을 비활성화할 수 없습니다."; // LOC-006 AC1 / LIFE-C1 AC1
			return new Decision(false, activeResourceCount, futureReservationCount, reason);
		}
		return new Decision(true, 0, futureReservationCount, null);
	}
}
