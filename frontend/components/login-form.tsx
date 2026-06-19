"use client";

import { useState } from "react";

// 로그인 폼(프레젠테이션). 실제 인증 호출은 onSubmit으로 주입받아 테스트를 쉽게 한다.
export function LoginForm({
  onSubmit,
}: {
  onSubmit: (loginId: string, password: string) => Promise<void>;
}) {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit(loginId, password);
    } catch {
      setError("아이디 또는 비밀번호가 올바르지 않습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3 w-72">
      <h1 className="text-lg font-semibold">로그인</h1>
      <label className="flex flex-col gap-1 text-sm">
        아이디
        <input
          aria-label="아이디"
          className="border rounded px-2 py-1"
          value={loginId}
          onChange={(e) => setLoginId(e.target.value)}
          autoComplete="username"
        />
      </label>
      <label className="flex flex-col gap-1 text-sm">
        비밀번호
        <input
          aria-label="비밀번호"
          type="password"
          className="border rounded px-2 py-1"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
        />
      </label>
      {error && <p role="alert" className="text-sm text-red-600">{error}</p>}
      <button
        type="submit"
        disabled={submitting}
        className="bg-black text-white rounded px-3 py-1.5 disabled:opacity-50"
      >
        로그인
      </button>
    </form>
  );
}
