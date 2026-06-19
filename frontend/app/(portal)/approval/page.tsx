"use client";

// 결재함 화면 골격(AP-010/033). 상신함/수신함/완료함 탭. 목록 데이터 연동은 백엔드 조회 API 단계에서.
import { useState } from "react";
import Link from "next/link";
import { RequireAuth } from "@/components/require-auth";

type Box = "DRAFTED" | "INBOX" | "DONE";

const TABS: { key: Box; label: string }[] = [
  { key: "DRAFTED", label: "상신함" },
  { key: "INBOX", label: "수신함" },
  { key: "DONE", label: "완료함" },
];

export default function ApprovalBoxPage() {
  const [box, setBox] = useState<Box>("INBOX");

  return (
    <RequireAuth>
      <div className="mx-auto max-w-3xl space-y-4 p-4">
        <div className="flex items-center justify-between">
          <h1 className="text-xl font-semibold">결재함</h1>
          <Link href="/approval/new" className="rounded border px-3 py-1 text-sm">
            결재 작성
          </Link>
        </div>
        <nav className="flex gap-2 border-b">
          {TABS.map((t) => (
            <button
              key={t.key}
              type="button"
              className={`px-3 py-1 text-sm ${
                box === t.key ? "border-b-2 border-black font-semibold" : "text-gray-500"
              }`}
              onClick={() => setBox(t.key)}
            >
              {t.label}
            </button>
          ))}
        </nav>
        <p className="text-sm text-gray-600">
          {TABS.find((t) => t.key === box)?.label} 목록(백엔드 조회 API 연동 예정)
        </p>
      </div>
    </RequireAuth>
  );
}
