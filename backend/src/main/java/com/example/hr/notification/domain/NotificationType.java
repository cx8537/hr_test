package com.example.hr.notification.domain;

/** 알림 유형(NOTI-002). 결재 6종 + 예약 2종. */
public enum NotificationType {
	APPROVAL_TURN,                 // 내 결재 차례 도달
	APPROVAL_APPROVED,             // 내가 상신한 문서 승인완료
	APPROVAL_REJECTED,             // 내가 상신한 문서 반려
	CONSENT_REQUEST,               // 합의 요청 도달
	APPROVAL_ON_HOLD,              // 합의 거부로 보류 발생
	DEPUTY_TURN,                   // 대결 대리인 처리 발생
	RESERVATION_CANCELLED_BY_ADMIN,// 예약이 관리자에 의해 취소
	RESERVATION_RESOURCE_REMOVED   // 예약 자원 삭제로 자동 취소
}
