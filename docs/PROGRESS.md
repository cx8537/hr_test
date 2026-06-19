# HR_Test_05 — 진행 상태 (PROGRESS)

이 파일은 Sidabari4Loop 자율 루프의 상태 원본이다. 매 턴 이 파일의 "다음 할 일"을 읽고 한 단계를 수행한 뒤, "진행 로그"에 한 줄을 append 하고 "다음 할 일"을 갱신한다. 상세 작업 순서는 `docs/BUILD_ORDER.md` 참고.

## 다음 할 일
- Phase 0-1: `/backend` 디렉토리에 Spring Boot 3.x (JDK 21, Gradle) 프로젝트 골격을 생성한다. 의존성: Spring Web, Spring Data JPA, Flyway, PostgreSQL driver, Validation, Spring Security, JWT 라이브러리, Lombok(선택). `application.yml`에 KST 타임존·`ddl-auto=validate`·환경변수 기반 DB/JWT/MinIO 설정을 외부화한다. `./gradlew build`가 통과하는지 확인한다(아직 기능 코드는 없음, 컴파일·기동만).

## 미해결
OPEN[01]: 결재 서명 라이브러리(JWT/JOSE) 및 빌드 도구 세부 선택은 기본값(jjwt + Gradle Kotlin DSL 아니면 Groovy DSL)으로 진행 예정. 사람이 특정 선호가 있으면 지정 요청.

## 진행 로그
- 2026-06-19: 셋업 턴. CLAUDE.md·docs 확인, PROGRESS.md / BUILD_ORDER.md 생성. 기능 개발은 미시작(사람 검수 후 자율 루프에서 시작).
