# E2E 인수 검증 셋업 (HR_Test_05)

`@docs/13-e2e-acceptance.md`의 시나리오를 4프로세스(PostgreSQL·Spring Boot·Next.js·MinIO) 가동 상태에서
브라우저(claude-in-chrome MCP / Playwright)로 검증하기 위한 **시드·키 주입·실행 절차**를 정의한다.

> 전제: `backend/src/main/resources/application-local.yml`에 DB·JWT·MinIO·부트스트랩 자격증명이 채워져야
> 백엔드가 기동된다(현재 미보유 → PROGRESS의 OPEN[01]). 이 문서는 자격증명 확보 시 그대로 실행 가능하도록 작성.

## 1. 페르소나 (결정론적 시드)

| 코드 | 이름 | login_id | 역할(범위) | 소속 |
|---|---|---|---|---|
| U  | 일반사용자 | `user.sales` | GENERAL | 영업부 |
| LM | 거점관리자 | `mgr.seoul` | LOCATION_MANAGER(서울 거점) | 영업부 |
| DM | 부서관리자 | `mgr.sales` | DEPT_MANAGER(영업부) | 영업부 |
| AM | 비품관리자 | `mgr.asset` | ASSET_MANAGER(서울 거점) | 영업부 |
| SA | 시스템관리자 | `admin` | SYS_ADMIN(전역) | 본사 |

- 초기 비밀번호는 시드 시 공통값(예: `Test1234`)으로 부여하고 `must_change_password=false`로 둔다(로그인 흐름 단축).
- SA는 부트스트랩 관리자(`BOOTSTRAP_ADMIN_*`)로 생성된 계정을 사용하거나 별도 시드.

## 2. 기준 데이터

- **부서**: 영업부, 개발부 (본사 하위)
- **거점**: 서울, 부산 (LOC-001)
- **자원**: 서울-차량1(VEHICLE, 행선지/운전자 필드), 서울-회의실1(MEETING_ROOM) (RSV-001)
- **양식**: 4종(지출/휴가/비품/일반)은 코드 정의(폼 빌더 없음)
- **공휴일**: 일부 등록(예: 2026-06-03 임시공휴일) — 휴가 일수 계산 검증용(AP-042)

## 3. 서명 키페어 주입 (서명 우회 금지)

- 페르소나별 RSA-2048 키페어를 **테스트 전용**으로 생성:
  - 공개키(SPKI Base64)는 `SignatureKey`로 시드 등록(상태 ACTIVE) → 서버 검증 통과(FND-008).
  - 개인키(PKCS8 PEM)는 픽스처 파일 `persona-keys/<persona>.pkcs8.pem`에 보관(운영과 분리).
- 승인 단계에서 자동화가 PEM을 **Web Crypto import → sign**해 실제 프론트 서명 경로(`lib/crypto.ts`)를 그대로 탄다.
  서명 payload는 `doc:{documentId}:r{round}:{approverId}` (백엔드 `ApprovalService.signaturePayload`와 동일).
- 키 재발급(FND-009) 검증용으로 한 페르소나는 옛/새 키 2개를 준비(E2E-AP-14).

## 4. 실행 순서

1. 테스트 스키마 초기화 → Flyway `V1~V13` 적용(`ddl-auto=validate`).
2. 시드 적용(위 1·2·3). 시각은 **고정 Clock + KST**.
3. 4프로세스 헬스체크 그린: PostgreSQL 5432 / MinIO 9000(버킷 생성) / Spring `/api/health` / Next dev 3000.
4. 브라우저 컨텍스트를 **페르소나별로 격리**(세션 교차오염 방지)하고 시나리오 수행.

## 5. 스모크 셋(최소 회귀 코어)

`E2E-AUTH-03 / AUTH-04 / AUTH-05 / RBAC-02 / AP-02 / AP-03 / AP-04 / RSV-02 / RSV-03 / DOC-01`

- 권한 "거부" 시나리오는 **네트워크 레벨 403**까지 확인(프론트 숨김만으로 통과 아님).
- 비동기(알림 생성, 결재 자동보관)는 **조건 대기/폴링**, 고정 sleep 금지.

## 6. 시나리오 카탈로그

전체 목록과 AC 매핑은 `@docs/13-e2e-acceptance.md` §5 참조(E2E-AUTH/RBAC/AP/LOC/AST/RSV/DOC/NOTI/LIFE/XROLE).

## 7. 현재 상태

- 프론트 라우트·화면 골격은 구현됨: `/login`, `(portal)/{dashboard,approval,approval/new,locations,assets,reservations,archive,notifications}`, `(admin)/admin/*`.
  포털 상단 네비게이션으로 역할별 진입점 확보(`(portal)/layout`).
- 백엔드 API·도메인 규칙·RBAC·서명·동시성(EXCLUSION/비관적 락)·알림 발행은 단위/통합(@Disabled) 테스트로 검증됨.
- **실 브라우저 역할별 E2E 통과는 백엔드 기동(자격증명) 필요 → OPEN[01].** 자격증명 확보 후 본 문서 절차대로 진행.
