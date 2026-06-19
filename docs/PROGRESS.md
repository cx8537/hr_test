# HR_Test_05 — 진행 상태 (PROGRESS)

이 파일은 Sidabari4Loop 자율 루프의 상태 원본이다. 매 턴 이 파일의 "다음 할 일"을 읽고 한 단계를 수행한 뒤, "진행 로그"에 한 줄을 append 하고 "다음 할 일"을 갱신한다. 상세 작업 순서는 `docs/BUILD_ORDER.md` 참고.

## 다음 할 일
- Phase 0-2: `/frontend` 디렉토리에 Next.js(App Router, **CSR**, TypeScript) 골격을 생성한다. `app/` 아래 `(portal)`·`(admin)` 라우트 그룹, `lib/api.ts` fetch 클라이언트(백엔드 8080 베이스 URL은 env로 외부화), 테스트 러너(Vitest 또는 Jest + Testing Library)를 설정한다. verify: `npm run build` 성공 + 빈/스모크 테스트 1개 통과.

## 미해결
- (없음)

## 진행 로그
- 2026-06-19: 셋업 턴. CLAUDE.md·docs 확인, PROGRESS.md / BUILD_ORDER.md 생성. 기능 개발은 미시작.
- 2026-06-19: Phase 0-1 완료. `/backend` Spring Boot 3.5.15(JDK21, Gradle Groovy DSL) 골격 생성 — 의존성(web/data-jpa/flyway/postgresql/validation/security/jjwt 0.12.6), `application.yml`에 KST·`ddl-auto=validate`·환경변수 외부화(DB/JWT/MinIO/부트스트랩), JVM 타임존 KST 고정. DB 의존 기본 테스트는 스모크 단위 테스트로 교체(컨텍스트 로드 검증은 DB 연결되는 0-3 이후). `gradlew build` 성공. OPEN[01] 해소: jjwt + Gradle Groovy DSL 확정, Lombok 미사용.
