// 포털(일반 업무 + 범위 관리) 레이아웃.
// 역할별 메뉴 동적 노출은 추후 단계에서 추가한다(권한 실제 강제는 백엔드 RBAC).
export default function PortalLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <section>{children}</section>;
}
