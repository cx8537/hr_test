// 포털(일반 업무 + 범위 관리) 레이아웃. 역할별 흐름 진입점(메뉴) 제공.
// 메뉴 노출은 UX 보조이며, 권한의 실제 강제는 백엔드 RBAC가 한다(FND-010).
import Link from "next/link";

const NAV = [
  { href: "/dashboard", label: "대시보드" },
  { href: "/approval", label: "결재함" },
  { href: "/locations", label: "거점" },
  { href: "/assets", label: "비품" },
  { href: "/reservations", label: "예약" },
  { href: "/archive", label: "문서함" },
  { href: "/notifications", label: "알림" },
];

export default function PortalLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen">
      <nav className="flex flex-wrap gap-3 border-b bg-gray-50 px-4 py-2 text-sm">
        {NAV.map((item) => (
          <Link key={item.href} href={item.href} className="text-gray-700 hover:underline">
            {item.label}
          </Link>
        ))}
      </nav>
      <section>{children}</section>
    </div>
  );
}
