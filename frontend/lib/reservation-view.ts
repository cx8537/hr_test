// 예약 화면 보조 순수 함수(RSV-003/004). 백엔드 TimeRangeOverlap과 동일 규칙(겹침 미리보기).
import type { ResourceType } from "./api";

interface Range {
  startAt: string;
  endAt: string;
}

/** 두 구간 [start,end) 겹침 판정. 맞닿음 허용(RSV-003 AC2). ISO 문자열 비교. */
export function overlaps(a: Range, b: Range): boolean {
  if (!(a.startAt < a.endAt) || !(b.startAt < b.endAt)) {
    throw new Error("종료 시각은 시작 시각보다 뒤여야 합니다.");
  }
  return a.startAt < b.endAt && b.startAt < a.endAt;
}

/** 신규 구간이 기존 예약들과 겹치는지(RSV-003 AC1). */
export function hasConflict(existing: Range[], candidate: Range): boolean {
  return existing.some((r) => overlaps(r, candidate));
}

/** 차량 유형이면 행선지·운전자 필드 노출(RSV-004 AC1/AC2). */
export function needsVehicleFields(type: ResourceType): boolean {
  return type === "VEHICLE";
}
