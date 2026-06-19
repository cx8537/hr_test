import { describe, expect, it } from "vitest";
import { notificationTypeLabel, unreadCount } from "@/lib/notification-view";
import type { NotificationResponse } from "@/lib/api";

const n = (id: number, read: boolean): NotificationResponse => ({
  id,
  type: "APPROVAL_TURN",
  message: "m",
  read,
  createdAt: "2026-06-19T00:00:00+09:00",
});

// NOTI-003: 유형 라벨·안읽음 집계.
describe("notification-view (NOTI-003)", () => {
  it("유형 라벨", () => {
    expect(notificationTypeLabel("APPROVAL_REJECTED")).toBe("반려");
    expect(notificationTypeLabel("UNKNOWN")).toBe("UNKNOWN");
  });

  it("AC1 안읽음 개수 집계", () => {
    expect(unreadCount([n(1, false), n(2, true), n(3, false)])).toBe(2);
  });
});
