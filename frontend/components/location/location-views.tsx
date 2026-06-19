"use client";

// 거점 현황 뷰(LOC-005): 목록/지도 토글. 두 뷰는 같은 데이터셋(props)에서 파생(AC2).
// 지도 API 키 미설정 시 마커를 좌표 목록으로 대체 표시(지도 키는 OPEN[01]).
import { useState } from "react";
import type { LocationResponse } from "@/lib/api";
import { toMarkers } from "@/lib/location-view";

type View = "LIST" | "MAP";

export default function LocationViews({ locations }: { locations: LocationResponse[] }) {
  const [view, setView] = useState<View>("LIST");
  const markers = toMarkers(locations);
  const mapKey = process.env.NEXT_PUBLIC_MAP_API_KEY;

  return (
    <div className="space-y-3">
      <nav className="flex gap-2 border-b">
        <button
          type="button"
          className={`px-3 py-1 text-sm ${view === "LIST" ? "border-b-2 border-black font-semibold" : "text-gray-500"}`}
          onClick={() => setView("LIST")}
        >
          목록
        </button>
        <button
          type="button"
          className={`px-3 py-1 text-sm ${view === "MAP" ? "border-b-2 border-black font-semibold" : "text-gray-500"}`}
          onClick={() => setView("MAP")}
        >
          지도
        </button>
      </nav>

      {view === "LIST" ? (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-gray-600">
              <th className="py-1">거점코드</th>
              <th>거점명</th>
              <th>주소</th>
              <th>좌표</th>
            </tr>
          </thead>
          <tbody>
            {locations.map((l) => (
              <tr key={l.id}>
                <td className="py-1">{l.locationCode}</td>
                <td>{l.name}</td>
                <td>{l.address ?? "-"}</td>
                <td className="tabular-nums">
                  {l.latitude !== null && l.longitude !== null
                    ? `${l.latitude}, ${l.longitude}`
                    : "미지정"}
                </td>
              </tr>
            ))}
            {locations.length === 0 && (
              <tr>
                <td colSpan={4} className="py-2 text-gray-500">
                  활성 거점이 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      ) : (
        <div className="rounded border p-3 text-sm">
          {mapKey ? (
            <div aria-label="map" className="h-64 bg-gray-100" />
          ) : (
            <p className="mb-2 text-gray-600">
              지도 API 키 미설정 — 좌표 목록으로 표시합니다.
            </p>
          )}
          <ul className="space-y-1">
            {markers.map((m) => (
              <li key={m.id}>
                📍 {m.name} ({m.latitude}, {m.longitude})
              </li>
            ))}
            {markers.length === 0 && <li className="text-gray-500">좌표가 지정된 거점이 없습니다.</li>}
          </ul>
        </div>
      )}
    </div>
  );
}
