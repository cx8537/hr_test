package com.example.hr.notification.domain;

/**
 * 이벤트 → 알림(대상·내용) 매핑(NOTI-002). 순수 함수.
 * 각 이벤트마다 지정 대상에게 알림 1건을 생성한다(AC1). 정상 흐름의 불필요한 알림은 호출 측이 억제(AC2).
 */
public final class NotificationFactory {

	private NotificationFactory() {
	}

	/** 내 결재 차례 도달 — 대상: 해당 결재자. */
	public static NotificationMessage approvalTurn(Long approverId, String title) {
		return new NotificationMessage(approverId, NotificationType.APPROVAL_TURN,
			"결재 요청이 도착했습니다: " + title);
	}

	/** 상신 문서 승인완료 — 대상: 상신자. */
	public static NotificationMessage approved(Long drafterId, String title) {
		return new NotificationMessage(drafterId, NotificationType.APPROVAL_APPROVED,
			"상신하신 문서가 승인완료되었습니다: " + title);
	}

	/** 상신 문서 반려(사유 포함) — 대상: 상신자. */
	public static NotificationMessage rejected(Long drafterId, String title, String reason) {
		return new NotificationMessage(drafterId, NotificationType.APPROVAL_REJECTED,
			"상신하신 문서가 반려되었습니다: " + title + " (사유: " + reason + ")");
	}

	/** 합의 요청 도달 — 대상: 합의자. */
	public static NotificationMessage consentRequest(Long consenterId, String title) {
		return new NotificationMessage(consenterId, NotificationType.CONSENT_REQUEST,
			"합의 요청이 도착했습니다: " + title);
	}

	/** 합의 거부로 보류 발생 — 대상: 상신자. */
	public static NotificationMessage onHold(Long drafterId, String title) {
		return new NotificationMessage(drafterId, NotificationType.APPROVAL_ON_HOLD,
			"합의 거부로 문서가 보류되었습니다: " + title);
	}

	/** 대결 대리인 처리 발생 — 대상: 대리인. */
	public static NotificationMessage deputyTurn(Long deputyId, String title) {
		return new NotificationMessage(deputyId, NotificationType.DEPUTY_TURN,
			"대결로 처리할 결재가 있습니다: " + title);
	}

	/** 예약이 관리자에 의해 취소(사유 포함) — 대상: 예약자. */
	public static NotificationMessage reservationCancelledByAdmin(Long reserverId,
			String resourceName, String reason) {
		return new NotificationMessage(reserverId, NotificationType.RESERVATION_CANCELLED_BY_ADMIN,
			"예약이 관리자에 의해 취소되었습니다: " + resourceName + " (사유: " + reason + ")");
	}

	/** 예약 자원 삭제로 자동 취소 — 대상: 예약자. */
	public static NotificationMessage reservationResourceRemoved(Long reserverId,
			String resourceName) {
		return new NotificationMessage(reserverId, NotificationType.RESERVATION_RESOURCE_REMOVED,
			"예약 자원이 삭제되어 예약이 자동 취소되었습니다: " + resourceName);
	}
}
