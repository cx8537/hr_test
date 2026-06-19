"use client";

import { useRouter } from "next/navigation";
import { LoginForm } from "@/components/login-form";
import { login } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();

  async function handleLogin(loginId: string, password: string) {
    const res = await login(loginId, password);
    // 최초 로그인·관리자 리셋 시 비밀번호 변경 강제(FND-001/003)
    router.replace(res.mustChangePassword ? "/change-password" : "/dashboard");
  }

  return (
    <main className="flex min-h-screen items-center justify-center">
      <LoginForm onSubmit={handleLogin} />
    </main>
  );
}
