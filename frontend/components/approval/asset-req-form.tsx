"use client";

// 비품 신청서 작성 폼(AP-044). 수령 거점 선택(비활성 거점 제외), 행별 예상 금액·합계 표시.
import { useState } from "react";
import { expenseAmount, expenseTotal } from "@/lib/approval-form";

interface LocationOption {
  id: number;
  name: string;
}

interface Row {
  itemName: string;
  quantity: number;
  unitPrice: number;
  note: string;
}

const EMPTY_ROW: Row = { itemName: "", quantity: 1, unitPrice: 0, note: "" };

export default function AssetReqForm({ locations = [] }: { locations?: LocationOption[] }) {
  const [receiveLocationId, setReceiveLocationId] = useState<number | "">("");
  const [rows, setRows] = useState<Row[]>([{ ...EMPTY_ROW }]);

  const update = (i: number, patch: Partial<Row>) =>
    setRows((rs) => rs.map((r, idx) => (idx === i ? { ...r, ...patch } : r)));
  const addRow = () => setRows((rs) => [...rs, { ...EMPTY_ROW }]);

  return (
    <div className="space-y-3 text-sm">
      <label className="block">
        수령 거점
        <select
          aria-label="수령 거점"
          className="ml-2 border px-1"
          value={receiveLocationId}
          onChange={(e) => setReceiveLocationId(e.target.value ? Number(e.target.value) : "")}
        >
          <option value="">선택</option>
          {locations.map((loc) => (
            <option key={loc.id} value={loc.id}>
              {loc.name}
            </option>
          ))}
        </select>
      </label>
      <table className="w-full">
        <thead>
          <tr className="text-left text-gray-600">
            <th>비품명</th>
            <th>수량</th>
            <th>예상단가</th>
            <th>금액</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r, i) => (
            <tr key={i}>
              <td>
                <input
                  aria-label={`비품명-${i}`}
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
                  aria-label={`예상단가-${i}`}
                  type="number"
                  className="w-24 border px-1"
                  value={r.unitPrice}
                  onChange={(e) => update(i, { unitPrice: Number(e.target.value) })}
                />
              </td>
              <td className="tabular-nums">
                {expenseAmount(r.quantity, r.unitPrice).toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <button type="button" className="rounded border px-2 py-1" onClick={addRow}>
        행 추가
      </button>
      <p className="text-right font-semibold">
        예상 합계: <span className="tabular-nums">{expenseTotal(rows).toLocaleString()}</span>
      </p>
    </div>
  );
}
