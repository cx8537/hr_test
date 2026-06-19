"use client";

// 지출결의서 작성 폼(AP-041). 행 추가, 금액=수량×단가 자동 표시, 합계 자동 합산.
import { useState } from "react";
import { expenseAmount, expenseTotal } from "@/lib/approval-form";

interface Row {
  itemName: string;
  quantity: number;
  unitPrice: number;
  note: string;
}

const EMPTY_ROW: Row = { itemName: "", quantity: 1, unitPrice: 0, note: "" };

export default function ExpenseForm() {
  const [rows, setRows] = useState<Row[]>([{ ...EMPTY_ROW }]);

  const update = (i: number, patch: Partial<Row>) =>
    setRows((rs) => rs.map((r, idx) => (idx === i ? { ...r, ...patch } : r)));
  const addRow = () => setRows((rs) => [...rs, { ...EMPTY_ROW }]);
  const removeRow = (i: number) =>
    setRows((rs) => (rs.length > 1 ? rs.filter((_, idx) => idx !== i) : rs));

  const total = expenseTotal(rows);

  return (
    <div className="space-y-3">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-gray-600">
            <th className="py-1">항목명</th>
            <th>수량</th>
            <th>단가</th>
            <th>금액</th>
            <th>비고</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r, i) => (
            <tr key={i}>
              <td className="py-1">
                <input
                  aria-label={`항목명-${i}`}
                  className="w-full border px-1"
                  value={r.itemName}
                  onChange={(e) => update(i, { itemName: e.target.value })}
                />
              </td>
              <td>
                <input
                  aria-label={`수량-${i}`}
                  type="number"
                  className="w-16 border px-1"
                  value={r.quantity}
                  onChange={(e) => update(i, { quantity: Number(e.target.value) })}
                />
              </td>
              <td>
                <input
                  aria-label={`단가-${i}`}
                  type="number"
                  className="w-24 border px-1"
                  value={r.unitPrice}
                  onChange={(e) => update(i, { unitPrice: Number(e.target.value) })}
                />
              </td>
              {/* 금액은 자동 계산(수정 불가, AC1) */}
              <td aria-label={`금액-${i}`} className="tabular-nums">
                {expenseAmount(r.quantity, r.unitPrice).toLocaleString()}
              </td>
              <td>
                <input
                  aria-label={`비고-${i}`}
                  className="w-full border px-1"
                  value={r.note}
                  onChange={(e) => update(i, { note: e.target.value })}
                />
              </td>
              <td>
                <button type="button" className="text-red-600" onClick={() => removeRow(i)}>
                  삭제
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <button type="button" className="rounded border px-2 py-1 text-sm" onClick={addRow}>
        행 추가
      </button>
      <p className="text-right font-semibold">
        합계: <span className="tabular-nums">{total.toLocaleString()}</span>
      </p>
    </div>
  );
}
