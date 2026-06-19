import { describe, expect, it } from "vitest";
import { previewQuantity, statusLabel, isDiscarded } from "@/lib/asset-view";

// AST-003: 입출고 미리보기 수량.
describe("previewQuantity (AST-003)", () => {
  it("입고는 가산", () => {
    expect(previewQuantity(10, "IN", 5)).toBe(15);
  });

  it("출고는 차감", () => {
    expect(previewQuantity(10, "OUT", 3)).toBe(7);
  });

  it("AC3 출고로 음수면 거부", () => {
    expect(() => previewQuantity(2, "OUT", 5)).toThrow();
  });

  it("음수 수량 거부", () => {
    expect(() => previewQuantity(10, "IN", -1)).toThrow();
  });
});

// AST-004: 폐기 상태 표시.
describe("status (AST-004)", () => {
  it("상태 라벨", () => {
    expect(statusLabel("USING")).toBe("사용중");
    expect(statusLabel("DISCARDED")).toBe("폐기");
  });

  it("폐기 여부", () => {
    expect(isDiscarded("DISCARDED")).toBe(true);
    expect(isDiscarded("USING")).toBe(false);
  });
});
