import { describe, expect, it } from "vitest";
import {
  expenseAmount,
  expenseTotal,
  leaveDays,
  isGeneralBodyValid,
  signaturePayload,
} from "@/lib/approval-form";

// AP-041: 지출 금액·합계 자동 계산.
describe("expense (AP-041)", () => {
  it("AC1 금액 = 수량 × 단가", () => {
    expect(expenseAmount(2, 1500000)).toBe(3000000);
  });

  it("AC2 합계 = 행 금액의 합", () => {
    expect(
      expenseTotal([
        { quantity: 2, unitPrice: 1500000 },
        { quantity: 3, unitPrice: 20000 },
      ]),
    ).toBe(3060000);
  });

  it("음수 수량/단가는 거부", () => {
    expect(() => expenseAmount(-1, 100)).toThrow();
    expect(() => expenseAmount(1, -100)).toThrow();
  });
});

// AP-042: 휴가 일수(영업일·공휴일 제외, 반차).
describe("leaveDays (AP-042)", () => {
  it("AC1 평일 5일(월~금)", () => {
    expect(leaveDays("2026-06-01", "2026-06-05", false)).toBe(5);
  });

  it("AC1 주말 제외", () => {
    // 2026-06-05(금)~06-08(월): 금·월만 영업일 = 2
    expect(leaveDays("2026-06-05", "2026-06-08", false)).toBe(2);
  });

  it("AC3 공휴일 제외", () => {
    expect(leaveDays("2026-06-01", "2026-06-05", false, ["2026-06-03"])).toBe(4);
  });

  it("AC2 반차 0.5일", () => {
    expect(leaveDays("2026-06-01", "2026-06-01", true)).toBe(0.5);
  });

  it("AC2 반차 복수일 거부", () => {
    expect(() => leaveDays("2026-06-01", "2026-06-02", true)).toThrow();
  });

  it("AC4 종료<시작 거부", () => {
    expect(() => leaveDays("2026-06-05", "2026-06-01", false)).toThrow();
  });
});

// AP-045: 일반 본문 필수.
describe("general body (AP-045)", () => {
  it("AC1 공백 본문은 무효", () => {
    expect(isGeneralBodyValid("   ")).toBe(false);
    expect(isGeneralBodyValid("내용")).toBe(true);
  });
});

// AP-034: 서명 payload 포맷이 백엔드와 일치.
describe("signaturePayload (AP-034)", () => {
  it("doc:{id}:r{round}:{approverId}", () => {
    expect(signaturePayload(7, 2, 100)).toBe("doc:7:r2:100");
  });
});
