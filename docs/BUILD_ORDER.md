# HR_Test_05 — 빌드 순서 (BUILD_ORDER)

CLAUDE.md의 범위를 **"턴 하나에 끝낼 수 있는 단계"**의 순서로 분해한 작업 정의다.
자율 루프는 매 턴 `docs/PROGRESS.md`의 "다음 할 일"을 수행하고, 끝나면 이 문서에서 다음 단계를 골라 갱신한다.

## 원칙 (CLAUDE.md 준수)
- **TDD 엄격 준수**: Red → Green → Refactor. 비즈니스 로직은 테스트 먼저, 테스트 없이 추가 금지.
- **모듈 의존 순서** (00-overview §3): `FND → (AP, LOC) → (AST, RSV, DOC) → (NOTI, LIFE) → E2E`.
  - AST는 FND·LOC 의존, RSV는 FND·LOC 의존, DOC는 FND·AP 의존, NOTI·LIFE는 전 모듈 의존.
- **Docker 미사용**, Windows 네이티브 4프로세스(PostgreSQL/Spring Boot/Next.js/MinIO).
- **스키마는 Flyway**로만, 운영 `ddl-auto=validate`. 시각은 **KST 고정**, 시간 의존 로직은 `Clock` 주입.
- **순수 도메인 로직**(09 §4 표)은 POJO로 분리해 DB 없이 단위 테스트. Controller/Repository는 얇게.
- 각 기능은 요구사항 ID(예: `AP-012`)를 테스트명·커밋에 참조.
- 한 단계 = "테스트 작성 → 통과 → 리팩터" 한 사이클로 끝낼 수 있는 크기. 큰 단계는 더 쪼개도 된다.

---

## Phase 0 — 프로젝트 골격 & 인프라
> 목표: 4개 프로세스가 로컬에서 뜨고, 빈 Spring Boot/Next.js가 서로 통신.

- **0-1** `/backend` Spring Boot 3.x(JDK21, Gradle) 골격 생성. 의존성(Web, JPA, Flyway, PostgreSQL, Validation, Security, JWT, JUnit5/AssertJ/Mockito). `application.yml`에 KST·`ddl-auto=validate`·환경변수 외부화. `gradlew build` 통과.
  - verify: `./gradlew build` 성공(기능 코드 없이 컴파일·기동만).
- **0-2** `/frontend` Next.js(App Router, **CSR**, TypeScript) 골격 생성. `(portal)`/`(admin)` 라우트 그룹, `lib/api.ts` fetch 클라이언트, 테스트 러너(Vitest/Jest + Testing Library) 설정.
  - verify: `npm run build` + 빈 테스트 1개 통과.
- **0-3** 로컬 인프라 준비 문서·스크립트: PostgreSQL 개발/테스트 스키마, MinIO(`minio.exe server`) 기동·버킷, 개발 CORS(3000→8080) 설정. `.env.example`/`application-local.yml.example` 작성(비밀값 커밋 금지).
  - verify: 백엔드가 로컬 PostgreSQL·MinIO에 연결 성공(헬스 체크).
- **0-4** Flyway 베이스라인 마이그레이션(`V1__init.sql`) + 공통 컬럼 규약(status, version, KST 타임스탬프). 11-data-model 기준 enum/상태값 정의.
  - verify: 백엔드 기동 시 Flyway 적용, `ddl-auto=validate` 통과.

## Phase 1 — FND: 인증·조직·권한 (의존: 없음)
> 문서: 01-foundation. 모든 후속 모듈의 토대.

- **1-1** 도메인 순수 로직 + 단위 테스트: 비밀번호 정책(FND-001), 부서 3단계 제한(FND-002), **RBAC 권한 판정**(FND-006: 역할×범위→허용/거부), 토큰 버전 무효화 로직.
- **1-2** Employee/Department/Role/EmployeeRoleScope 엔티티 + Flyway 마이그레이션 + Repository(통합 테스트). 최초 시스템관리자 시드(`BOOTSTRAP_ADMIN_*`).
- **1-3** 로그인·토큰(FND-003): Access+Refresh 발급, 최초 로그인 비밀번호 변경 강제. 통합 테스트.
- **1-4** 토큰 검증 필터(FND-004): 매 요청 계정상태·토큰버전 검증(비활성/퇴사/로그아웃 즉시 차단). 부정 테스트 포함.
- **1-5** RBAC API 강제(FND-010) + 역할 매 요청 판정(권한 변경 즉시 반영). 범위 외 호출 403 부정 테스트.
- **1-6** 임직원·부서·역할 관리 API(CRUD, 비활성화는 소프트 삭제) + 프론트 로그인/관리 화면 + 라우트 가드.

## Phase 2 — FND: 결재 서명 키 (의존: Phase 1)
> 문서: 01-foundation FND-007~009. AP보다 먼저 필요(승인=서명).

- **2-1** 서명 검증 순수 로직(FND-008): (데이터, 서명, 공개키)→유효성. RSA-2048 단위 테스트(프론트 Web Crypto 호환).
- **2-2** SignatureKey 엔티티 + 키 발급(FND-007: 공개키만 DB 저장, 개인키 1회 다운로드) + 폐기·재발급(FND-009, 과거 서명 검증 가능). 서명마다 **공개키 ID 기록**.
- **2-3** 프론트 키 생성·서명 흐름(Web Crypto) + 백엔드 공개키 검증 호환성 테스트(양쪽 동일 알고리즘).

## Phase 3 — AP: 결재 핵심 흐름 (의존: FND)
> 문서: 02-approval. 가장 복잡 — 흐름 엔진을 먼저 순수 함수로.

- **3-1** **결재 흐름 엔진** 순수 로직 + 단위 테스트: 순차(AP-010), 병렬 전원승인(AP-011), 병렬 1인반려 즉시전체반려(AP-012), 합의 차단형 보류/재개(AP-013). 상태표: 임시저장→진행중→보류→승인완료|반려|회수(AP-033).
- **3-2** 전결 결재선 생성기(AP-020) + 지출 합계 계산(AP-041) 순수 로직·단위 테스트.
- **3-3** **휴가 일수 계산기**(AP-042): 주말·공휴일 제외, 반차 0.5일, `Clock`/공휴일 주입. 경계값 단위 테스트.
- **3-4** ApprovalDocument/ApprovalLineSnapshot/Holiday 엔티티 + 마이그레이션. **상신 시 결재선 스냅샷 고정**(AP-002).
- **3-5** 결재선 자동 적용·수정(AP-001) + 상신 + 승인 시 서명 필수(AP-034). 흐름 엔진을 Service에서 조립.
- **3-6** 상태 전이 동시성(AP-033): **비관적 락**(SELECT FOR UPDATE)+상태 재검증. 동시 승인/회수 일관성 통합 테스트.
- **3-7** 회수(AP-030: 무승인만), 반려 사유필수(AP-031), 재상신(AP-032). 부정 테스트(1승인 후 회수 거부).

## Phase 4 — AP: 위임·대결·양식 (의존: Phase 3)
- **4-1** 대결(AP-021: 런타임 판정, 대리인 본인 키 서명) + 위임(AP-022: 수동 해제까지, 수임자 본인 키, 위임자 명의). Delegation/Mandate 엔티티.
- **4-2** 결재 양식 4종: 지출결의서(AP-041), 휴가·근태(AP-042), 비품 신청서(AP-044, 수령거점), 일반 품의서(AP-045). 공휴일 관리 API(AP-043).
- **4-3** 첨부(AP-040): MinIO 저장 + 메타 DB, **백엔드 경유 접근**(presigned 금지). 프론트 결재 작성·결재함 화면.

## Phase 5 — LOC: 거점 (의존: FND)
> 문서: 03-location.

- **5-1** 거점 비활성화 가능 판정(LOC-006/LIFE-C1) 순수 로직 단위 테스트.
- **5-2** Location/LocationPhoto 엔티티 + CRUD(LOC-001), 좌표(LOC-002), 다중 사진 MinIO(LOC-004), 담당자/권한 분리(LOC-003). 거점관리자 범위 RBAC 403.
- **5-3** 거점 현황 화면(LOC-005: 지도/목록 토글, 활성만) + 지도 핀 드롭.

## Phase 6 — AST: 비품 (의존: FND, LOC)
> 문서: 04-asset.

- **6-1** 수량 산출 순수 로직(AST-003): 입출고 이력→현재수량, 음수 방지 경계값 단위 테스트.
- **6-2** AssetItem/IndividualAsset/QuantityAsset/StockTransaction 엔티티 + 개체(AST-002: 관리번호 유니크) vs 수량(AST-003: 이력) 분리. 폐기 상태 보존(AST-004).
- **6-3** 비품 CRUD·사진(AST-005) API + 비품관리자 범위 RBAC. 결재 연동 없음(AST-006) 확인. 프론트 화면.

## Phase 7 — RSV: 예약 (의존: FND, LOC)
> 문서: 05-reservation.

- **7-1** 시간 겹침 판정 순수 로직(RSV-003): 맞닿음 허용/포함/부분겹침 경계값 단위 테스트.
- **7-2** Resource/Reservation/ResourcePhoto 엔티티 + **EXCLUSION 제약**(GiST+tstzrange) 마이그레이션. 동시 2요청 한 건만 성공 통합 테스트.
- **7-3** 즉시 예약(RSV-002) + 유형별 필드(RSV-004: 차량 행선지·운전자) + 취소(RSV-005: 본인 자유/관리자 사유+알림). 프론트 예약 화면.

## Phase 8 — DOC: 문서 아카이브 (의존: FND, AP)
> 문서: 06-document-archive.

- **8-1** 공개범위 판정 순수 로직(DOC-005): (공개범위, 요청자, 관여자)→허용/거부. 관여자=상신자+결재자(+합의/대결/수임)+시스템관리자. 부정 테스트.
- **8-2** ArchiveDocument/ArchiveVersion/Folder/Tag 엔티티 + 결재완료/반려/회수 시 **자동 보관**(DOC-002, 도메인 이벤트). 일반 업로드(DOC-001).
- **8-3** 분류(DOC-003 폴더+태그), 검색(DOC-004 파일명·제목만), 버전(DOC-006), **백엔드 경유 다운로드**(DOC-007). 비관여자 403 통합 테스트. 프론트 아카이브 화면.

## Phase 9 — NOTI: 알림 (의존: 전 모듈)
> 문서: 07-notification.

- **9-1** 이벤트→알림 매핑 순수 로직(NOTI-002, 7개 이벤트). Notification 엔티티.
- **9-2** 도메인 이벤트 구독 핸들러(결재 차례/완료/반려/합의/보류, 예약 취소, 자원 삭제). 읽음/안읽음·뱃지·폴링(NOTI-003). 통합 테스트.

## Phase 10 — LIFE: 라이프사이클·예외 (의존: 전 모듈)
> 문서: 08-lifecycle-rules.

- **10-1** 비활성화 가능 판정들 + 시스템관리자 최소 1명 보장(LIFE-A5) 순수 로직·부정 테스트.
- **10-2** 퇴사 처리 트랜잭션(LIFE-A1~A3: 계정 비활성+키 폐기+영향 결재 산출) + 진행중 결재선 교체 안내(LIFE-A2).
- **10-3** 부서 비활성화 직원 재배치(LIFE-B1/B2), 거점 비활성화 자원 정리(LIFE-C1), 자원 삭제 미래 예약 자동취소+알림(LIFE-C3/RSV-007). 전역규칙: 비활성 대상 신규 선택지 제외.

## Phase 11 — E2E 인수 검증 (의존: 전 모듈)
> 문서: 13-e2e-acceptance. 브라우저 검증은 claude-in-chrome MCP 사용.

- **11-1** 페르소나 픽스처(U/LM/DM/AM/SA) + 테스트 키 주입 시드 데이터.
- **11-2** 스모크 셋 회귀: E2E-AUTH-03/04/05, RBAC-02, AP-02/03/04, RSV-02/03, DOC-01.
- **11-3** 1순위 검증: 권한 경계(API 403), 민감 문서 비관여자 403, 결재 흐름+키 서명. 브라우저로 역할별 흐름 확인.

---

## 진행 규칙 (자율 루프)
- 한 턴에 한 단계(또는 그 일부)만. 끝나면 `docs/PROGRESS.md` "진행 로그"에 한 줄 append, "다음 할 일" 갱신.
- 핵심 결정이 필요하면 멈추지 말고 합리적 기본값으로 진행하되 `PROGRESS.md`에 `OPEN[NN]:`로 남긴다.
- 모든 작업 완료 시에만 `PROGRESS.md`에 `TASK_COMPLETE` 마커를 쓴다.
