"use client";

// 알림함 화면(NOTI-003). 본인 알림 목록 + 안읽음 수 뱃지 + 읽음 처리.
import { useEffect, useState } from "react";
import { RequireAuth } from "@/components/require-auth";
import {
  listNotifications,
  markNotificationRead,
  type NotificationResponse,
} from "@/lib/api";
import { notificationTypeLabel, unreadCount } from "@/lib/notification-view";

export default function NotificationsPage() {
  const [items, setItems] = useState<NotificationResponse[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = () =>
    listNotifications()
      .then(setItems)
      .catch(() => setError("알림을 불러오지 못했습니다."));

  useEffect(() => {
    load();
  }, []);

  const onRead = async (id: number) => {
    await markNotificationRead(id);
    setItems((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
  };

  return (
    <RequireAuth>
      <div className="mx-auto max-w-3xl space-y-4 p-4">
        <h1 className="text-xl font-semibold">
          알림 <span className="text-sm text-red-600">({unreadCount(items)} 안읽음)</span>
        </h1>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <ul className="divide-y">
          {items.map((n) => (
            <li key={n.id} className="flex items-center justify-between py-2 text-sm">
              <span className={n.read ? "text-gray-400" : "font-medium"}>
                [{notificationTypeLabel(n.type)}] {n.message}
              </span>
              {!n.read && (
                <button
                  type="button"
                  className="rounded border px-2 py-0.5 text-xs"
                  onClick={() => onRead(n.id)}
                >
                  읽음
                </button>
              )}
            </li>
          ))}
          {items.length === 0 && <li className="py-2 text-gray-500">알림이 없습니다.</li>}
        </ul>
      </div>
    </RequireAuth>
  );
}
