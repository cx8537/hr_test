"use client";

// 일반 품의서 작성 폼(AP-045). 본문 필수(AC1) — 비어 있으면 상신 비활성.
import { useState } from "react";
import { isGeneralBodyValid } from "@/lib/approval-form";

export default function GeneralForm() {
  const [body, setBody] = useState("");
  const valid = isGeneralBodyValid(body);

  return (
    <div className="space-y-2 text-sm">
      <textarea
        aria-label="본문"
        className="h-40 w-full border p-2"
        placeholder="품의 내용을 입력하세요."
        value={body}
        onChange={(e) => setBody(e.target.value)}
      />
      {!valid && <p className="text-red-600">본문은 필수입니다.</p>}
    </div>
  );
}
