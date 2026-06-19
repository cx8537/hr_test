import { test, expect, request } from "@playwright/test";

/**
 * 스모크 회귀 셋(@docs/13-e2e-acceptance.md §5).
 * 실행 전제: 백엔드(8080, local 프로파일)·프론트(3000) 기동 + 부트스트랩 관리자(admin/admin1234) 시드.
 *
 * 핵심 인증/가드/RBAC 시나리오는 실제 실행한다. 별도 페르소나(LM/DM/AM)나
 * 결재 서명·동시성처럼 추가 시드/암호화가 필요한 케이스는 사유와 함께 test.skip 으로 둔다
 * (동시성은 백엔드 ReservationExclusionIT, 서명은 SignatureVerifierWebCryptoCompatTest 로 커버됨).
 */

const API = process.env.E2E_API_BASE ?? "http://localhost:8080";
const ADMIN = { loginId: "admin", password: "admin1234" };

test.describe("스모크: 인증·가드", () => {
  test("E2E-AUTH-GUARD: 미인증 포털 접근 → /login 리다이렉트 (FND-010 AC1)", async ({ page }) => {
    await page.context().clearCookies();
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/\/login/);
  });

  test("E2E-AUTH-LOGIN: 부트스트랩 관리자 로그인 → 비밀번호 변경 강제 (FND-003)", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("아이디").fill(ADMIN.loginId);
    await page.getByLabel("비밀번호").fill(ADMIN.password);
    await page.getByRole("button", { name: "로그인" }).click();
    // 최초 로그인은 must_change_password=true → /change-password
    await expect(page).toHaveURL(/\/change-password/);
  });

  test("E2E-AUTH-BADPW: 잘못된 비밀번호 → 오류 표시·미이동", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("아이디").fill(ADMIN.loginId);
    await page.getByLabel("비밀번호").fill("wrong-pw");
    await page.getByRole("button", { name: "로그인" }).click();
    await expect(page.getByRole("alert")).toBeVisible();
    await expect(page).toHaveURL(/\/login/);
  });
});

test.describe("스모크: RBAC 경계 (API)", () => {
  test("E2E-RBAC: 관리 API는 미인증 차단, 관리자 토큰은 허용 (FND-006/010)", async () => {
    const ctx = await request.newContext();

    // 미인증 → 차단(403)
    const anon = await ctx.post(`${API}/api/admin/departments`, {
      data: { deptCode: "E2E-ANON", name: "anon", parentId: null },
    });
    expect(anon.status()).toBe(403);

    // 관리자 로그인 → 토큰 발급
    const login = await ctx.post(`${API}/api/auth/login`, { data: ADMIN });
    expect(login.status()).toBe(200);
    const token = (await login.json()).accessToken as string;
    expect(token).toBeTruthy();

    // 관리자 토큰 → 부서 생성 허용(200)
    const ok = await ctx.post(`${API}/api/admin/departments`, {
      headers: { Authorization: `Bearer ${token}` },
      data: { deptCode: `E2E-${Date.now() % 100000}`, name: "E2E부서", parentId: null },
    });
    expect(ok.status()).toBe(200);
    expect((await ok.json()).status).toBe("ACTIVE");

    await ctx.dispose();
  });
});

const NEED_PERSONA = "추가 페르소나/시드 필요 — 핵심 경계는 위 테스트와 백엔드 IT로 커버";

test.describe("스모크: 추가 페르소나 의존(시드 후 활성화)", () => {
  // 범위 권한: LM(서울)이 부산 거점 편집 → 403. LM 페르소나 시드 필요.
  test.skip("E2E-RBAC-02: 범위 밖 거점 편집 거부 (FND-006 AC2)", () => {});
  // 결재 서명: Web Crypto 서명 → 서버 검증. 키 시드+서명 흐름 필요(서명 검증은 백엔드 단위테스트로 커버).
  test.skip("E2E-AP-02: 승인 서명 서버 검증 (AP-034·FND-008)", () => {});
  // 동시성: 동시 2요청 한 건만 성공. 백엔드 ReservationExclusionIT(EXCLUSION)로 커버.
  test.skip("E2E-RSV-03: 동시 예약 한 건만 성공 (RSV-003 AC3)", () => {});
  void NEED_PERSONA;
});
