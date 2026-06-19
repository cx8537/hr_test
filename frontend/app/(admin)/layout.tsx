"use client";

import Link from "next/link";
import { RequireAuth } from "@/components/require-auth";

// 시스템 관리 레이아웃. 미인증 시 라우트 가드로 보호(UX 보조).
// 권한의 실제 강제는 백엔드 RBAC가 담당한다(FND-010).
export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <RequireAuth>
      <div className="flex min-h-screen">
        <nav className="w-48 border-r p-4 flex flex-col gap-2 text-sm">
          <span className="font-semibold">시스템 관리</span>
          <Link href="/admin/employees">임직원</Link>
          <Link href="/admin/departments">부서</Link>
          <Link href="/admin/role-scopes">역할·권한</Link>
        </nav>
        <section className="flex-1 p-6">{children}</section>
      </div>
    </RequireAuth>
  );
}
