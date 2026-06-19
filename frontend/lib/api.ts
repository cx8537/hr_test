// 백엔드 REST API fetch 클라이언트 (CSR 중심).
// 백엔드 베이스 URL은 환경변수로 외부화한다(포트 다름 → 개발 CORS 필요).
import { clearTokens, getAccessToken, setTokens } from "./auth";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

// 공통 fetch 래퍼. JSON 요청/응답을 기본으로 하고, 토큰이 있으면 Authorization 헤더를 첨부한다.
export async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token = getAccessToken();
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (!res.ok) {
    throw new ApiError(res.status, `API 요청 실패: ${res.status}`);
  }

  // 204 No Content 등 본문 없는 응답 처리
  if (res.status === 204) {
    return undefined as T;
  }
  return (await res.json()) as T;
}

// --- 인증 API (FND-003/004) ---
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  mustChangePassword: boolean;
}

export async function login(loginId: string, password: string): Promise<LoginResponse> {
  const res = await apiFetch<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ loginId, password }),
  });
  setTokens(res.accessToken, res.refreshToken);
  return res;
}

export async function logout(): Promise<void> {
  try {
    await apiFetch<void>("/api/auth/logout", { method: "POST" });
  } finally {
    clearTokens();
  }
}

export { API_BASE_URL };
