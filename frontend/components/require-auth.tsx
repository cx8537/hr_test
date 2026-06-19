"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { isAuthenticated } from "@/lib/auth";

/**
 * 라우트 가드(UX 보조). 미인증 시 /login으로 리다이렉트한다.
 * 실제 권한 경계는 백엔드 RBAC가 강제한다(FND-010, NFR-SEC-004).
 */
export function RequireAuth({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [authed, setAuthed] = useState<boolean | null>(null);

  useEffect(() => {
    if (isAuthenticated()) {
      setAuthed(true);
    } else {
      setAuthed(false);
      router.replace("/login");
    }
  }, [router]);

  if (authed !== true) {
    return null; // 판정 전/미인증: 화면 비노출
  }
  return <>{children}</>;
}
