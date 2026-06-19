import { describe, expect, it } from "vitest";
import { overlaps, hasConflict, needsVehicleFields } from "@/lib/reservation-view";

const r = (startAt: string, endAt: string) => ({ startAt, endAt });

// RSV-003: 겹침 미리보기(맞닿음 허용).
describe("reservation overlap (RSV-003)", () => {
  it("분리되면 겹치지 않음", () => {
    expect(overlaps(r("2026-06-20T09:00", "2026-06-20T10:00"), r("2026-06-20T11:00", "2026-06-20T12:00"))).toBe(false);
  });

  it("AC2 맞닿음 허용", () => {
    expect(overlaps(r("2026-06-20T09:00", "2026-06-20T10:00"), r("2026-06-20T10:00", "2026-06-20T11:00"))).toBe(false);
  });

  it("AC1 부분겹침 충돌", () => {
    expect(overlaps(r("2026-06-20T09:00", "2026-06-20T11:00"), r("2026-06-20T10:00", "2026-06-20T12:00"))).toBe(true);
  });

  it("기존 목록과 충돌 검사", () => {
    const existing = [r("2026-06-20T09:00", "2026-06-20T10:00"), r("2026-06-20T13:00", "2026-06-20T14:00")];
    expect(hasConflict(existing, r("2026-06-20T13:30", "2026-06-20T15:00"))).toBe(true);
    expect(hasConflict(existing, r("2026-06-20T10:00", "2026-06-20T11:00"))).toBe(false);
  });

  it("역전 구간 거부", () => {
    expect(() => overlaps(r("2026-06-20T11:00", "2026-06-20T09:00"), r("2026-06-20T12:00", "2026-06-20T13:00"))).toThrow();
  });
});

// RSV-004: 차량 폼 분기.
describe("needsVehicleFields (RSV-004)", () => {
  it("차량은 행선지/운전자 필요", () => {
    expect(needsVehicleFields("VEHICLE")).toBe(true);
    expect(needsVehicleFields("MEETING_ROOM")).toBe(false);
  });
});
