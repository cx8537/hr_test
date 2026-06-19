import { defineConfig, devices } from "@playwright/test";

/**
 * E2E 스모크 회귀 설정(@docs/13-e2e-acceptance.md). Vitest와 분리된 별도 러너.
 * 실행 전 4프로세스 기동 + 시드(docs/E2E_SETUP.md) 필요 → 현재 OPEN[01]로 미실행.
 * 페르소나별 storageState는 시드 후 글로벌 셋업에서 로그인해 저장한다(자리만 표기).
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: 0,
  reporter: "list",
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "http://localhost:3000",
    trace: "on-first-retry",
  },
  projects: [
    // 페르소나별 컨텍스트 격리(세션 교차오염 방지). storageState는 글로벌 셋업 산출물.
    { name: "chromium", use: { ...devices["Desktop Chrome"] } },
  ],
});
