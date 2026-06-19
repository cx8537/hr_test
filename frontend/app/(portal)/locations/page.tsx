"use client";

// 거점 현황 화면(LOC-005). 활성 거점만 조회해 목록/지도 토글로 표시.
import { useEffect, useState } from "react";
import { RequireAuth } from "@/components/require-auth";
import LocationViews from "@/components/location/location-views";
import { listLocations, type LocationResponse } from "@/lib/api";

export default function LocationsPage() {
  const [locations, setLocations] = useState<LocationResponse[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listLocations()
      .then(setLocations)
      .catch(() => setError("거점 목록을 불러오지 못했습니다."));
  }, []);

  return (
    <RequireAuth>
      <div className="mx-auto max-w-3xl space-y-4 p-4">
        <h1 className="text-xl font-semibold">거점 현황</h1>
        {error ? (
          <p className="text-sm text-red-600">{error}</p>
        ) : (
          <LocationViews locations={locations} />
        )}
      </div>
    </RequireAuth>
  );
}
