"use client";

// 비품 화면 골격(AST). 거점 선택 → 품목 목록, 개체 상태(폐기 포함), 수량 품목 입출고 폼.
// 목록/입출고 실제 연동은 백엔드 조회 API·인증 필요(OPEN[01]). 여기서는 작성 폼·미리보기 중심.
import { useState } from "react";
import { RequireAuth } from "@/components/require-auth";
import { previewQuantity } from "@/lib/asset-view";
import type { StockTransactionType } from "@/lib/api";

export default function AssetsPage() {
  const [current] = useState(0);
  const [type, setType] = useState<StockTransactionType>("IN");
  const [quantity, setQuantity] = useState(0);

  let preview = "";
  try {
    preview = String(previewQuantity(current, type, quantity));
  } catch (e) {
    preview = e instanceof Error ? e.message : "오류";
  }

  return (
    <RequireAuth>
      <div className="mx-auto max-w-3xl space-y-4 p-4">
        <h1 className="text-xl font-semibold">비품 관리</h1>
        <p className="text-sm text-gray-600">
          거점별 비품 품목·개체·수량 관리(목록 연동 예정). 결재 연동 없음(AST-006).
        </p>

        <section className="space-y-2 rounded border p-3 text-sm">
          <h2 className="font-semibold">수량 입출고</h2>
          <p>현재 수량: <span className="tabular-nums">{current}</span></p>
          <div className="flex items-center gap-2">
            <select
              aria-label="입출고 유형"
              className="border px-1"
              value={type}
              onChange={(e) => setType(e.target.value as StockTransactionType)}
            >
              <option value="IN">입고</option>
              <option value="OUT">출고</option>
            </select>
            <input
              aria-label="수량"
              type="number"
              className="w-24 border px-1"
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
            />
            <span>→ 예상 수량: <span aria-label="예상 수량">{preview}</span></span>
          </div>
        </section>
      </div>
    </RequireAuth>
  );
}
