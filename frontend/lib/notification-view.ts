// 알림 화면 보조 순수 함수(NOTI-003). 유형 라벨·안읽음 집계.
import type { NotificationResponse } from "./api";

const TYPE_LABELS: Record<string, string> = {
  APPROVAL_TURN: "결재 요청",
  APPROVAL_APPROVED: "승인 완료",
  APPROVAL_REJECTED: "반려",
  CONSENT_REQUEST: "합의 요청",
  APPROVAL_ON_HOLD: "보류",
  DEPUTY_TURN: "대결 요청",
  RESERVATION_CANCELLED_BY_ADMIN: "예약 취소",
  RESERVATION_RESOURCE_REMOVED: "예약 자동취소",
};

export function notificationTypeLabel(type: string): string {
  return TYPE_LABELS[type] ?? type;
}

/** 안읽음 개수(NOTI-003 AC1). */
export function unreadCount(notifications: NotificationResponse[]): number {
  return notifications.filter((n) => !n.read).length;
}
