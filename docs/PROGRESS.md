# HR_Test_05 — 진행 상태 (PROGRESS)

이 파일은 Sidabari4Loop 자율 루프의 상태 원본이다. 매 턴 이 파일의 "다음 할 일"을 읽고 한 단계를 수행한 뒤, "진행 로그"에 한 줄을 append 하고 "다음 할 일"을 갱신한다. 상세 작업 순서는 `docs/BUILD_ORDER.md` 참고.

## 다음 할 일
- Phase 0-3: 로컬 인프라 준비. PostgreSQL 개발/테스트 스키마 생성 가이드, MinIO(`minio.exe server`) 기동·버킷 생성 가이드, 개발 CORS(3000→8080) 설정을 백엔드에 추가한다. `backend/application-local.yml.example`(DB/JWT/MinIO 실제값 placeholder, 비밀값 커밋 금지)과 인프라 셋업 문서(`docs/INFRA_SETUP.md` 또는 README)를 작성한다. verify: 백엔드가 로컬 PostgreSQL·MinIO에 연결 성공하는 헬스 체크(가능 시), 최소한 CORS 설정·예시 파일·문서 완비.

## 미해결
- (없음)

## 진행 로그
- 2026-06-19: 셋업 턴. CLAUDE.md·docs 확인, PROGRESS.md / BUILD_ORDER.md 생성. 기능 개발은 미시작.
- 2026-06-19: Phase 0-1 완료. `/backend` Spring Boot 3.5.15(JDK21, Gradle Groovy DSL) 골격 생성 — 의존성(web/data-jpa/flyway/postgresql/validation/security/jjwt 0.12.6), `application.yml`에 KST·`ddl-auto=validate`·환경변수 외부화(DB/JWT/MinIO/부트스트랩), JVM 타임존 KST 고정. DB 의존 기본 테스트는 스모크 단위 테스트로 교체(컨텍스트 로드 검증은 DB 연결되는 0-3 이후). `gradlew build` 성공. OPEN[01] 해소: jjwt + Gradle Groovy DSL 확정, Lombok 미사용.
- 2026-06-19: Phase 0-2 완료. `/frontend` Next.js 16.2.9(App Router, CSR, TypeScript, Tailwind v4) 골격 생성 — `(portal)/dashboard`·`(admin)/admin` 라우트 그룹, `lib/api.ts`(fetch 클라이언트, 백엔드 URL env 외부화), `.env.local.example`. 테스트 러너 Vitest + Testing Library 설정, 스모크 테스트 추가. `npm run build` 성공(/, /admin, /dashboard 라우트 생성) + `npm run test` 통과.
