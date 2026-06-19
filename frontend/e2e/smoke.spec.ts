import { test, expect } from "@playwright/test";

/**
 * 스모크 회귀 셋(@docs/13-e2e-acceptance.md §5, 최소 회귀 코어).
 * AUTH-03/04/05, RBAC-02, AP-02/03/04, RSV-02/03, DOC-01.
 *
 * 각 시나리오는 명시적 기대결과(상태 전이 / HTTP 403 / 요소 유무)로 pass/fail 한다.
 * "거부" 시나리오는 네트워크 레벨 403까지 확인(프론트 숨김만으로 통과 아님).
 *
 * 현재 4프로세스 미기동(자격증명 OPEN[01])이라 전 케이스 test.skip 으로 골격만 둔다.
 * 시드(docs/E2E_SETUP.md) + 백엔드 기동 후 skip 제거하며 단계적으로 활성화한다.
 */

const SKIP_REASON = "백엔드 기동·시드 필요(OPEN[01]) — 자격증명 확보 시 활성화";

test.describe("스모크: 인증·권한 경계", () => {
  test.skip("E2E-AUTH-03: 일반사용자가 관리 라우트 직접 접근 → 차단/리다이렉트 (FND-010 AC1)", async () => {
    // given U 로그인 → when /admin URL 직접 진입 → then /login 또는 403, 관리 화면 비노출
    expect(SKIP_REASON).toBeTruthy();
  });

  test.skip("E2E-AUTH-04: 로그인 중 거점관리자 부여 → 새로고침 시 관리 메뉴 즉시 노출, 회수 시 제거 (FND-006 AC4)", async () => {
    // 역할은 토큰 비포함·매 요청 판정 → 권한 변경 즉시 반영
    expect(SKIP_REASON).toBeTruthy();
  });

  test.skip("E2E-AUTH-05: 세션 중 사용자 비활성화 → 다음 요청 즉시 401 (FND-004 AC3)", async () => {
    expect(SKIP_REASON).toBeTruthy();
  });
});

test.describe("스모크: 범위 권한", () => {
  test.skip("E2E-RBAC-02: LM(서울)이 부산 거점 편집 → 403 (FND-006 AC2)", async () => {
    // 관리자 API를 해당 토큰으로 직접 호출해 403까지 확인(라우트 가드 우회 불가)
    expect(SKIP_REASON).toBeTruthy();
  });
});

test.describe("스모크: 전자결재", () => {
  test.skip("E2E-AP-02: 승인 시 키 서명 → 서버 검증 통과 → 진행 (AP-034·FND-008)", async () => {
    // persona-keys PEM을 Web Crypto import→sign, payload doc:{id}:r{round}:{approverId}
    expect(SKIP_REASON).toBeTruthy();
  });

  test.skip("E2E-AP-03: 순차 전원 승인 → 승인완료 → 아카이브 자동보관 (AP-010·DOC-002)", async () => {
    expect(SKIP_REASON).toBeTruthy();
  });

  test.skip("E2E-AP-04: 병렬 1인 반려 → 즉시 전체 반려 (AP-012)", async () => {
    expect(SKIP_REASON).toBeTruthy();
  });
});

test.describe("스모크: 예약", () => {
  test.skip("E2E-RSV-02: 겹침 예약 거부 / 경계 맞닿음 허용 (RSV-003 AC1·2)", async () => {
    expect(SKIP_REASON).toBeTruthy();
  });

  test.skip("E2E-RSV-03: 동시 2요청 → 한 건만 성공 (EXCLUSION, RSV-003 AC3)", async () => {
    expect(SKIP_REASON).toBeTruthy();
  });
});

test.describe("스모크: 문서 아카이브 권한", () => {
  test.skip("E2E-DOC-01: 휴가 문서 비관여자 조회/다운로드 → 403 (DOC-005 AC1)", async () => {
    expect(SKIP_REASON).toBeTruthy();
  });
});
