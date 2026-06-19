// 거점 현황 화면 보조 순수 함수(LOC-005). 지도/목록 뷰가 같은 데이터셋을 쓰도록 변환만 담당(AC2).
import type { LocationResponse } from "./api";

export interface Marker {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
}

/** 좌표가 지정된 거점인지(LOC-002 핀 여부). */
export function hasCoordinates(loc: LocationResponse): boolean {
  return loc.latitude !== null && loc.longitude !== null;
}

/** 목록 데이터셋 → 지도 마커. 좌표 없는 거점은 마커에서 제외(동일 원본에서 파생). */
export function toMarkers(locations: LocationResponse[]): Marker[] {
  return locations
    .filter(hasCoordinates)
    .map((l) => ({
      id: l.id,
      name: l.name,
      latitude: l.latitude as number,
      longitude: l.longitude as number,
    }));
}
