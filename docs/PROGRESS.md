# HR_Test_05 — 진행 상태 (PROGRESS)

이 파일은 Sidabari4Loop 자율 루프의 상태 원본이다. 매 턴 이 파일의 "다음 할 일"을 읽고 한 단계를 수행한 뒤, "진행 로그"에 한 줄을 append 하고 "다음 할 일"을 갱신한다. 상세 작업 순서는 `docs/BUILD_ORDER.md` 참고.

## 다음 할 일
- Phase 0-4: Flyway 베이스라인 마이그레이션 `backend/src/main/resources/db/migration/V1__init.sql` 작성. 공통 컬럼 규약(status, version[낙관적 잠금], KST 타임스탬프 created_at/updated_at)과 11-data-model 기준 enum/상태값(ACTIVE/INACTIVE/REVOKED 등)을 정의한다. 실제 테이블은 모듈별 후속 마이그레이션에서 추가하므로 V1은 공통 타입/도메인(또는 최소 스키마 메타)만 둔다. verify: SQL 문법 정합성 확인. (기동 시 Flyway 적용·`ddl-auto=validate` 통과는 DB 자격증명 확보 후 — OPEN[01] 참조.)

## 미해결
OPEN[01]: 로컬 PostgreSQL(superuser/`hr` 계정) 및 MinIO 자격증명이 없어 `application-local.yml`을 채울 수 없다. 사람이 `backend/src/main/resources/application-local.yml`(예시 파일 복사)에 DB 비밀번호·JWT 시크릿·MinIO 키·부트스트랩 관리자 초기 비밀번호를 입력해야 백엔드 실기동·Flyway 적용·`/api/health` 실연결 검증이 가능하다. PostgreSQL 18(5432)·MinIO(9000)는 가동 중 확인됨.

## 진행 로그
- 2026-06-19: 셋업 턴. CLAUDE.md·docs 확인, PROGRESS.md / BUILD_ORDER.md 생성. 기능 개발은 미시작.
- 2026-06-19: Phase 0-1 완료. `/backend` Spring Boot 3.5.15(JDK21, Gradle Groovy DSL) 골격 생성 — 의존성(web/data-jpa/flyway/postgresql/validation/security/jjwt 0.12.6), `application.yml`에 KST·`ddl-auto=validate`·환경변수 외부화(DB/JWT/MinIO/부트스트랩), JVM 타임존 KST 고정. DB 의존 기본 테스트는 스모크 단위 테스트로 교체(컨텍스트 로드 검증은 DB 연결되는 0-3 이후). `gradlew build` 성공. OPEN[01] 해소: jjwt + Gradle Groovy DSL 확정, Lombok 미사용.
- 2026-06-19: Phase 0-2 완료. `/frontend` Next.js 16.2.9(App Router, CSR, TypeScript, Tailwind v4) 골격 생성 — `(portal)/dashboard`·`(admin)/admin` 라우트 그룹, `lib/api.ts`(fetch 클라이언트, 백엔드 URL env 외부화), `.env.local.example`. 테스트 러너 Vitest + Testing Library 설정, 스모크 테스트 추가. `npm run build` 성공(/, /admin, /dashboard 라우트 생성) + `npm run test` 통과.
- 2026-06-19: Phase 0-3 완료(코드·문서). 개발 CORS+Security 설정(`SecurityConfig`, 골격 단계 permitAll·Phase1에서 RBAC 교체 예정), 헬스 엔드포인트(`HealthController` /api/health, DB ping), `application.yml`에 `app.cors.allowed-origins` 추가, `application-local.yml.example` 및 `.gitignore`(local yml 제외), `docs/INFRA_SETUP.md`(PostgreSQL/MinIO/기동순서) 작성. `gradlew build` 성공. PostgreSQL18(5432)·MinIO(9000) 가동 확인. 실DB 연결 헬스체크는 자격증명 미보유로 보류 → OPEN[01].
