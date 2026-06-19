"use client";

// 최초 로그인/관리자 리셋 후 비밀번호 변경 화면 골격(FND-001/003).
// 변경 폼·API 연동은 후속 단계에서 구현.
export default function ChangePasswordPage() {
  return (
    <main className="flex min-h-screen items-center justify-center">
      <div className="w-72">
        <h1 className="text-lg font-semibold">비밀번호 변경</h1>
        <p className="mt-2 text-sm text-gray-600">
          최초 로그인 시 비밀번호 변경이 필요합니다(후속 구현).
        </p>
      </div>
    </main>
  );
}
