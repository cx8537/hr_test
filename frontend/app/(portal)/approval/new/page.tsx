"use client";

// 결재 작성 화면(AP-001/041/042/044/045). 양식 선택 후 양식별 본문 입력.
// 상신(결재선 적용)·서명 흐름은 백엔드 결재 컨트롤러 연동 단계에서 마무리(OPEN[01]).
import { useState } from "react";
import { RequireAuth } from "@/components/require-auth";
import ExpenseForm from "@/components/approval/expense-form";
import LeaveForm from "@/components/approval/leave-form";
import AssetReqForm from "@/components/approval/asset-req-form";
import GeneralForm from "@/components/approval/general-form";
import type { FormType } from "@/lib/approval-form";

const FORM_LABELS: Record<FormType, string> = {
  EXPENSE: "지출결의서",
  LEAVE: "휴가·근태 신청서",
  ASSET_REQ: "비품 신청서",
  GENERAL: "일반 품의서",
};

export default function NewApprovalPage() {
  const [formType, setFormType] = useState<FormType>("EXPENSE");
  const [title, setTitle] = useState("");

  return (
    <RequireAuth>
      <div className="mx-auto max-w-3xl space-y-4 p-4">
        <h1 className="text-xl font-semibold">결재 작성</h1>
        <label className="block text-sm">
          양식
          <select
            aria-label="양식"
            className="ml-2 border px-1"
            value={formType}
            onChange={(e) => setFormType(e.target.value as FormType)}
          >
            {(Object.keys(FORM_LABELS) as FormType[]).map((t) => (
              <option key={t} value={t}>
                {FORM_LABELS[t]}
              </option>
            ))}
          </select>
        </label>
        <label className="block text-sm">
          제목
          <input
            aria-label="제목"
            className="ml-2 w-96 border px-1"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </label>

        <section className="rounded border p-3">
          {formType === "EXPENSE" && <ExpenseForm />}
          {formType === "LEAVE" && <LeaveForm />}
          {formType === "ASSET_REQ" && <AssetReqForm />}
          {formType === "GENERAL" && <GeneralForm />}
        </section>
      </div>
    </RequireAuth>
  );
}
