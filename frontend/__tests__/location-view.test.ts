import { describe, expect, it } from "vitest";
import { hasCoordinates, toMarkers } from "@/lib/location-view";
import type { LocationResponse } from "@/lib/api";

function loc(over: Partial<LocationResponse>): LocationResponse {
  return {
    id: 1,
    locationCode: "HQ",
    name: "본사",
    address: null,
    latitude: null,
    longitude: null,
    contact: null,
    fax: null,
    locationType: null,
    managerId: null,
    status: "ACTIVE",
    ...over,
  };
}

// LOC-005: 지도/목록 뷰가 동일 데이터셋에서 파생되는지.
describe("location-view (LOC-005)", () => {
  it("좌표 있으면 hasCoordinates true", () => {
    expect(hasCoordinates(loc({ latitude: 37.5, longitude: 127 }))).toBe(true);
    expect(hasCoordinates(loc({}))).toBe(false);
  });

  it("AC2 좌표 있는 거점만 마커로 변환(동일 원본)", () => {
    const list = [
      loc({ id: 1, name: "본사", latitude: 37.5, longitude: 127 }),
      loc({ id: 2, name: "창고", latitude: null, longitude: null }),
    ];
    const markers = toMarkers(list);
    expect(markers).toHaveLength(1);
    expect(markers[0]).toEqual({ id: 1, name: "본사", latitude: 37.5, longitude: 127 });
  });
});
