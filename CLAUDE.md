# CLAUDE.md

사내 통합 업무관리 시스템(전자결재·거점·비품·예약·문서 아카이브). 이 파일은 매 세션에 로드되는 핵심 규칙만 담는다. 상세 명세는 `/docs`에 있으며, 작업 시 관련 문서를 `@docs/파일명.md`로 참조한다.

## 기술 스택 (고정)
- 프론트엔드: Next.js (App Router), **CSR 중심, SSR 사용 안 함**
- 백엔드: Spring Boot 3.x, JDK 21 LTS, Gradle
- DB: PostgreSQL, Spring Data JPA (+ 필요 시 native query)
- 인증: JWT 토큰 기반
- 파일 저장: MinIO (Windows 네이티브 바이너리, **Docker 사용 안 함**), 모든 접근은 백엔드 경유
- 결재 서명: RSA-2048, 브라우저 Web Crypto 서명 → 서버 공개키 검증
- 개발 환경: Windows 11. 로컬 4개 프로세스(PostgreSQL, Spring Boot, Next.js, MinIO)

## 절대 규칙
- **Docker를 쓰지 않는다.** 모든 구성요소는 Windows에 네이티브로 설치·실행.
- **개인키는 절대 서버에 저장하지 않는다.** 결재 서명용 공개키만 저장한다.
- **파일은 presigned URL로 직접 노출하지 않는다.** 반드시 백엔드가 권한 검사 후 전달.
- **삭제 대신 비활성화.** 계정·부서·거점은 물리 삭제 금지(소프트 삭제/비활성). 상세: `@docs/08-lifecycle-rules.md`
- 민감 양식(휴가·근태)과 반려·회수 문서는 **관여자/관리자만** 열람. 상세: `@docs/06-document-archive.md`

## 개발 방식: TDD (필수)
- **Red → Green → Refactor**를 엄격히 따른다. 테스트를 먼저 쓰고, 실패를 확인한 뒤, 통과시키는 최소 구현을 한다.
- 모든 기능 요구사항에는 인수 기준(Acceptance Criteria)이 `@docs`에 명시되어 있다. 각 기준은 최소 1개의 테스트로 매핑한다.
- 비즈니스 규칙(결재 흐름, 일수 계산, 권한)은 **순수 함수/도메인 서비스로 분리**해 단위 테스트가 쉽도록 만든다.
- 테스트 없이 비즈니스 로직을 추가하지 않는다.
- 상세 전략: `@docs/09-testing-strategy.md`

## 아키텍처 원칙
- 계층 분리: Controller → Service(도메인 로직) → Repository. 도메인 로직은 프레임워크에 의존하지 않게.
- 모듈 경계: 각 도메인(approval, org, location, asset, reservation, document, auth, notification)을 독립 패키지로. 모듈 간 결합 최소화.
- 결재선은 **상신 시 스냅샷**으로 고정한다(구조 고정, 실제 처리 권한은 위임/대결 런타임 판정). 상세: `@docs/02-approval.md`
- 각 서명에는 **사용된 공개키 ID를 함께 기록**한다(키 재발급 후에도 과거 서명 검증 가능).
- UI는 **단일 앱 + 역할 기반 라우트 가드**, 권한의 실제 강제는 백엔드 RBAC. 역할은 토큰에 넣지 않고 **매 요청 판정**(권한 변경 즉시 반영). 상세: `@docs/01-foundation.md`(FND-010)
- 인증은 **Access+Refresh 토큰**, 매 요청 계정상태·토큰버전 검증(비활성·퇴사·로그아웃 즉시 차단).
- 동시성은 자원별: 예약=EXCLUSION 제약, 결재 상태=비관적 락, 일반 편집=낙관적 잠금. 시각은 **KST 고정**. 스키마는 **Flyway** 관리.

## 문서 맵 (/docs)
- `00-overview.md` — 시스템 전체 개요, 모듈 목록, 용어집
- `01-foundation.md` — 사용자·조직·권한(RBAC)·인증·결재키
- `02-approval.md` — 전자결재(결재선·흐름·전결/대결/위임·양식)
- `03-location.md` — 거점 관리
- `04-asset.md` — 비품 관리
- `05-reservation.md` — 예약 관리(차량·회의실)
- `06-document-archive.md` — 문서 아카이브
- `07-notification.md` — 알림
- `08-lifecycle-rules.md` — 상태 변화·예외 규칙(퇴사·부서개편·폐쇄·반려 등)
- `09-testing-strategy.md` — TDD 전략·모듈화·테스트 매핑
- `10-nonfunctional.md` — 비기능 요구사항(보안·성능·운영)
- `11-data-model.md` — 데이터 모델(엔티티·관계)
- `12-dev-environment.md` — Windows 11 로컬 개발 환경 구축
- `13-e2e-acceptance.md` — 시스템 가동 후 역할별 E2E 인수 검증(Playwright MCP, 테스트 키 주입)

## 작업 규칙
- 새 기능은 해당 `@docs` 문서의 요구사항 ID(예: AP-012)를 커밋·테스트명에 참조한다.
- 요구사항과 구현이 어긋나면 임의 해석하지 말고 문서를 확인하고, 모호하면 질문한다.
- 한국어로 소통한다. 코드 주석은 필요한 곳에만 간결하게.
