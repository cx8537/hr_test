// 시스템 관리 레이아웃. 역할 가드(라우트 보호)는 추후 단계에서 추가한다.
// 권한의 실제 강제는 백엔드 RBAC가 담당하며, 여기 가드는 보조 수단이다(FND-010).
export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <section>{children}</section>;
}
