# HR_Test_05 — 구현 현황 (STATUS)

사내 통합 업무관리 시스템. Phase 0~11 구현 완료(코드·테스트). 실 기동/통합·E2E 검증은 자격증명(OPEN[01]) 확보 후 수행.

## 기술 스택
- 프론트: Next.js 16 (App Router, CSR), TypeScript, Tailwind v4, Vitest
- 백엔드: Spring Boot 3.5.x, JDK 21, Gradle, Spring Data JPA, Flyway, Spring Security, jjwt 0.12.6
- DB: PostgreSQL 18 / 파일: MinIO(네이티브, 백엔드 경유, presigned 미사용) / 결재 서명: RSA-2048 Web Crypto↔서버 검증

## 모듈별 구현·테스트 매핑

| 모듈 | 핵심 구현 | 순수 도메인(단위) | 서비스/통합 |
|---|---|---|---|
| FND 인증·조직·권한 | 비밀번호 정책·부서 3단계·RBAC(역할×범위 매 요청)·JWT(Access+Refresh, tv)·토큰필터 | PasswordPolicy, DepartmentTree, RbacPolicy, TokenVersionValidator | AuthService, AuthorizationService, JwtAuthenticationFilter, Dept/Employee/RoleScopeService |
| FND 서명키 | RSA-2048 검증(Web Crypto 호환), 발급/폐기/재발급(공개키만 저장) | SignatureVerifier | SignatureKeyService, SignatureValidationService |
| AP 결재 | 흐름엔진(순차/병렬/합의/전결)·전결선·지출/휴가 계산·스냅샷·서명·대결/위임·양식4종·공휴일·첨부 | ApprovalFlowEngine, PrerogativeLineGenerator, ExpenseCalculator, LeaveDaysCalculator, DelegationResolver | ApprovalService(락·서명·재상신·회수), FormBodyService, HolidayService, AttachmentService, ApprovalController |
| LOC 거점 | 거점 CRUD·좌표·담당자·사진, 비활성화(자원 잔존 검사) | LocationDeactivation | LocationService, LocationPhotoService, LocationController |
| AST 비품 | 개체(상태·폐기 보존)/수량(입출고 이력→현재수량) 분리·사진 | StockCalculator | AssetService, AssetPhotoService, AssetResourceCounter, AssetController |
| RSV 예약 | 자원·예약(즉시확정)·취소·EXCLUSION 동시성·차량 필드·자원삭제 자동취소 | TimeRangeOverlap | ReservationService, ResourceService, ReservationController |
| DOC 아카이브 | 공개범위 판정·자동보관·폴더/태그·검색·버전·백엔드 경유 다운로드 | DocumentAccessPolicy, VisibilityMapper | ArchiveService, DocumentArchiveService, DocumentController |
| NOTI 알림 | 이벤트→알림 매핑(8종)·알림함·안읽음/읽음·결재/예약 발행 연동 | NotificationFactory | NotificationService, NotificationController |
| LIFE 라이프사이클 | 시스템관리자 최소1명 보장·퇴사(비활성+키폐기+영향결재)·부서 비활성화 직원 잔존 거부 | SysAdminGuard | LifecycleService, LifecycleController |

프론트 화면: `/login`, `/change-password`, `(admin)/admin/{employees,departments,role-scopes}`,
`(portal)/{dashboard,approval,approval/new,locations,assets,reservations,archive,notifications}` + 상단 네비게이션.

## 스키마(Flyway)
V1 베이스라인 → V2 FND → V3 refresh_token → V4 signature_key → V5 approval_core → V6 delegation/mandate →
V7 approval_forms → V8 attachment → V9 location → V10 asset → V11 reservation(EXCLUSION) → V12 document → V13 notification.

## 테스트
- 백엔드: `cd backend && .\gradlew.bat test` — 순수 도메인 + 서비스(Mockito) + 통합 IT 모두 그린(236). 통합(`*IT`)은 실 PostgreSQL/MinIO 사용(`local` 프로파일): `EmployeeRepositoryIT`(영속성·auditing), `ReservationExclusionIT`(예약 EXCLUSION RSV-003), `MinioFileStorageIT`(파일 왕복).
- 프론트: `cd frontend && npm run test`(Vitest 37) + `npm run build`. E2E: `npm run test:e2e`(백엔드 8080·프론트 3000 기동 후) — `e2e/smoke.spec.ts` 4 passed(인증가드·로그인·잘못된비번·RBAC) / 3 skip(페르소나·서명·동시성, 백엔드 IT·단위로 커버).

## 실행 방법(요약)
1. `docs/12-dev-environment.md`·`docs/INFRA_SETUP.md`로 PostgreSQL·MinIO 준비.
2. `backend/src/main/resources/application-local.yml`에 DB/JWT/MinIO/부트스트랩 자격증명 입력(예시 파일 복사).
3. 백엔드 `gradlew bootRun` → 프론트 `npm run dev` → 4프로세스 헬스 그린.
4. E2E: `docs/E2E_SETUP.md` 절차(페르소나·테스트 키 시드)대로 진행.

## 검증 완료 (구 OPEN[01] 해소)
- 로컬 PostgreSQL/MinIO 자격증명을 확보해 `application-local.yml` 구성, `hr` DB/롤/`btree_gist` 생성. 실 기동에서 Flyway V1~V13 적용·`ddl-auto=validate`·`/api/health` UP 확인.
- 실DB/실MinIO 통합 테스트(IT) + Playwright 스모크 + Chrome MCP 라이브 검증까지 완료. 이 과정에서 버그 4건(JPA auditing / MinIO 버킷명 `hr`→`hr-files` + 자동생성 / 포털 인증가드 / refresh 토큰 BCrypt→SHA-256) 발견·수정.
- MinIO 버킷명은 S3 규칙(3~63자)상 `hr-files` 사용.
