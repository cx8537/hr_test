"use client";

// 휴가·근태 신청서 작성 폼(AP-042). 기간·반차 입력 시 총 일수 자동 표시(영업일·공휴일 제외).
import { useState } from "react";
import { leaveDays } from "@/lib/approval-form";

export default function LeaveForm({ holidays = [] }: { holidays?: string[] }) {
  const [leaveType, setLeaveType] = useState("ANNUAL");
  const [start, setStart] = useState("");
  const [end, setEnd] = useState("");
  const [halfDay, setHalfDay] = useState(false);

  let days = "";
  if (start && end) {
    try {
      days = String(leaveDays(start, end, halfDay, holidays));
    } catch (e) {
      days = e instanceof Error ? e.message : "계산 오류";
    }
  }

  return (
    <div className="space-y-3 text-sm">
      <label className="block">
        휴가 종류
        <select
          aria-label="휴가 종류"
          className="ml-2 border px-1"
          value={leaveType}
          onChange={(e) => setLeaveType(e.target.value)}
        >
          <option value="ANNUAL">연차</option>
          <option value="HALF">반차</option>
          <option value="SICK">병가</option>
          <option value="FAMILY">경조사</option>
          <option value="OFFICIAL">공가</option>
          <option value="ETC">기타</option>
        </select>
      </label>
      <label className="block">
        시작일
        <input
          aria-label="시작일"
          type="date"
          className="ml-2 border px-1"
          value={start}
          onChange={(e) => setStart(e.target.value)}
        />
      </label>
      <label className="block">
        종료일
        <input
          aria-label="종료일"
          type="date"
          className="ml-2 border px-1"
          value={end}
          onChange={(e) => setEnd(e.target.value)}
        />
      </label>
      <label className="block">
        <input
          aria-label="반차"
          type="checkbox"
          className="mr-2"
          checked={halfDay}
          onChange={(e) => setHalfDay(e.target.checked)}
        />
        반차(0.5일, 단일 날짜)
      </label>
      <p className="font-semibold">
        총 일수: <span aria-label="총 일수">{days}</span>
      </p>
    </div>
  );
}
