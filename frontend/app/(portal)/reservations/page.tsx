"use client";

// 예약 화면 골격(RSV-002/004). 자원 유형에 따라 차량 행선지·운전자 필드 조건 렌더.
// 자원 목록·예약 제출 실제 연동은 인증·백엔드 필요(OPEN[01]).
import { useState } from "react";
import { RequireAuth } from "@/components/require-auth";
import { needsVehicleFields } from "@/lib/reservation-view";
import type { ResourceType } from "@/lib/api";

export default function ReservationsPage() {
  const [type, setType] = useState<ResourceType>("MEETING_ROOM");
  const [startAt, setStartAt] = useState("");
  const [endAt, setEndAt] = useState("");

  return (
    <RequireAuth>
      <div className="mx-auto max-w-3xl space-y-4 p-4">
        <h1 className="text-xl font-semibold">예약</h1>
        <p className="text-sm text-gray-600">
          빈 시간대를 선택하면 즉시 확정됩니다(승인 없음, RSV-002).
        </p>

        <section className="space-y-2 rounded border p-3 text-sm">
          <label className="block">
            자원 유형
            <select
              aria-label="자원 유형"
              className="ml-2 border px-1"
              value={type}
              onChange={(e) => setType(e.target.value as ResourceType)}
            >
              <option value="MEETING_ROOM">회의실</option>
              <option value="VEHICLE">차량</option>
            </select>
          </label>
          <label className="block">
            시작
            <input
              aria-label="시작"
              type="datetime-local"
              className="ml-2 border px-1"
              value={startAt}
              onChange={(e) => setStartAt(e.target.value)}
            />
          </label>
          <label className="block">
            종료
            <input
              aria-label="종료"
              type="datetime-local"
              className="ml-2 border px-1"
              value={endAt}
              onChange={(e) => setEndAt(e.target.value)}
            />
          </label>

          {needsVehicleFields(type) && (
            <div className="space-y-2 rounded bg-gray-50 p-2">
              <label className="block">
                행선지
                <input aria-label="행선지" className="ml-2 border px-1" />
              </label>
              <label className="block">
                운전자
                <input aria-label="운전자" className="ml-2 border px-1" />
              </label>
            </div>
          )}
        </section>
      </div>
    </RequireAuth>
  );
}
