# 11. 데이터 모델 (개념)

엔티티와 관계의 개념 설계. 실제 컬럼 타입·제약은 구현 시 확정하되, 아래 구조와 핵심 제약을 따른다. (모든 엔티티는 생성일시·수정일시 공통 보유)

> **스키마는 Flyway 버전드 마이그레이션으로 관리**한다(운영 `ddl-auto=validate`). 동시 수정 가능성이 있는 주요 엔티티(Location·AssetItem·Resource 등)는 **낙관적 잠금용 `version` 컬럼**을 보유한다. 모든 시각 컬럼은 KST(Asia/Seoul) 기준(`10`·`12` 참조).

---

## 1. 토대 (FND)

### Employee (임직원)
- id(PK), emp_no(고유), login_id(고유), password_hash, name, dept_id(FK→Department), position, email, phone, status(ACTIVE/INACTIVE), token_version(기본 0), must_change_password(bool)
- 제약: emp_no·login_id 유니크. dept_id 필수(1인 1부서).
- token_version: 로그아웃·퇴사·강제만료 시 증가시켜 기존 Access 토큰 무효화(FND-004). must_change_password: 최초 생성·관리자 리셋 시 true(최초 로그인 시 변경 강제).

### RefreshToken (리프레시 토큰)
- id(PK), employee_id(FK), token_hash, issued_at, expires_at, revoked(bool)
- 로그아웃·퇴사 시 revoked 처리. Access 토큰 무효화는 Employee.token_version 증가로 처리.

### Department (부서)
- id(PK), dept_code(고유), name, parent_id(FK→Department, nullable), level(1~3), status(ACTIVE/INACTIVE)
- 제약: level ≤ 3.

### Role / EmployeeRoleScope (역할·범위)
- Role: 코드(GENERAL, DEPT_MANAGER, LOCATION_MANAGER, ASSET_MANAGER, SYS_ADMIN)
- EmployeeRoleScope: id(PK), employee_id(FK), role_code, scope_type(NONE/DEPT/LOCATION), scope_id(nullable)
  - 예: (홍길동, LOCATION_MANAGER, LOCATION, LOC-서울)
- 한 직원이 여러 역할·범위를 가짐(다대다 + 범위).

### SignatureKey (결재 공개키)
- id(PK=공개키 ID), employee_id(FK), public_key(PEM), status(ACTIVE/REVOKED), created_at, revoked_at(nullable)
- 한 직원이 시점별로 여러 키 보유 가능(재발급). 폐기 키도 보존.

---

## 2. 전자결재 (AP)

### ApprovalDocument (결재 문서)
- id(PK), form_type(EXPENSE/LEAVE/ASSET_REQ/GENERAL), title, drafter_id(FK→Employee), draft_dept_id(FK→Department, 상신 시 스냅샷), status(DRAFT/IN_PROGRESS/ON_HOLD/APPROVED/REJECTED/WITHDRAWN), created_at
- form_type별 상세 필드는 별도 테이블 또는 JSON 본문(아래).

### ApprovalLineSnapshot (결재선 스냅샷)
- id(PK), document_id(FK), step_no, approver_id(FK→Employee), step_type(SEQUENTIAL/PARALLEL/CONSENT), state(PENDING/APPROVED/REJECTED/CONSENT_REJECTED/SKIPPED), acted_at
- **상신 시 복사 고정**. 이후 조직 변경 무관.

### ApprovalSignature (서명)
- id(PK), document_id(FK), step_no, signer_id(FK→Employee), public_key_id(FK→SignatureKey), signature(서명값), on_behalf_of_id(nullable, 대결/위임 시 원 결재자), signed_at
- **public_key_id 필수**(검증 추적).

### Delegation (대결 대리인 사전 지정)
- id(PK), approver_id(FK), deputy_id(FK), active(bool)

### Mandate (위임)
- id(PK), mandator_id(FK 위임자), mandatee_id(FK 수임자), active(bool), created_at, released_at(nullable)
- 기간 없음(수동 해제). 표시: 위임자 명의 + 수임자 대행.

### FormTypeConfig (양식 설정)
- form_type(PK), prerogative_step(전결 종료 단계), disclosure_scope(PUBLIC/RESTRICTED), amount_condition(nullable, 미사용 확장 여지)

### 양식별 본문
- ExpenseDoc: document_id(FK), pay_due_date, vendor_info / ExpenseLine: id, document_id, item_name, qty, unit_price, amount(=qty×unit_price), note
- LeaveDoc: document_id, leave_type, start_date, end_date, half_day_part(AM/PM/null), total_days, reason, substitute_id(FK→Employee)
- AssetRequestDoc: document_id, desired_receive_date, receive_location_id(FK→Location), reason / AssetRequestLine: 유사 다중행
- GeneralDoc: document_id, body(text)

### Holiday (공휴일)
- id(PK), date(고유), name

### Attachment (첨부)
- id(PK), document_id(FK), file_name, size, minio_object_key, uploader_id, uploaded_at
- minio_object_key는 참조ID(document_id 등)를 포함하는 규칙으로 생성(고아 정리 추적용). 업로드는 **MinIO 저장 성공 후 메타 커밋**(파일 먼저→DB 커밋, `10` NFR-DATA 참조).

---

## 3. 거점 (LOC)

### Location (거점)
- id(PK), loc_code(고유), name, address, latitude, longitude, phone, fax, manager_id(FK→Employee, 1명), loc_type(HQ/BRANCH/WAREHOUSE...), status(ACTIVE/INACTIVE)

### LocationPhoto
- id(PK), location_id(FK), minio_object_key, order

---

## 4. 비품 (AST)

### AssetItem (비품 — 공통)
- id(PK), name, location_id(FK), manage_type(INDIVIDUAL/QUANTITY)

### IndividualAsset (개체 — manage_type=INDIVIDUAL)
- asset_item_id(FK 또는 통합), asset_no(고유), state(IN_USE/STORED/REPAIR/DISCARDED), acquired_date
- **상태 덮어쓰기**(이력 테이블 없음).

### QuantityAsset (수량 — manage_type=QUANTITY)
- asset_item_id(FK), current_qty(파생)
### StockTransaction (입출고 이력)
- id(PK), asset_item_id(FK), type(IN/OUT), qty, occurred_at, actor_id
- current_qty = Σ(IN) − Σ(OUT).

### AssetPhoto
- id(PK), owner_type(INDIVIDUAL/ITEM), owner_id, minio_object_key

---

## 5. 예약 (RSV)

### Resource (자원)
- id(PK), name, type(VEHICLE/MEETING_ROOM), location_id(FK), operating_hours, status(ACTIVE/INACTIVE)
- 유형별: MeetingRoom(equipment) / Vehicle(model, plate_no) — 서브타입 또는 nullable 컬럼.

### ResourcePhoto
- id(PK), resource_id(FK), minio_object_key

### Reservation (예약)
- id(PK), resource_id(FK), reserver_id(FK→Employee), start_at, end_at, purpose, headcount, destination(차량), driver(차량), note, status(CONFIRMED/CANCELLED), cancel_reason(nullable), cancelled_by(nullable)
- 제약: 동일 resource_id에 시간대 겹침 금지를 **EXCLUSION 제약(GiST + tstzrange, `&&` 연산)**으로 보장한다. 경계 맞닿음(끝시각=다음 시작시각)은 겹침이 아니다(`[)` 반열림 구간). 동시 요청에도 DB가 한 건만 통과시킨다(RSV-003).
- 인덱스: EXCLUSION 제약이 생성하는 GiST 인덱스(resource_id + tstzrange).

---

## 6. 문서 아카이브 (DOC)

### ArchiveDocument
- id(PK), source_type(APPROVAL/UPLOAD), approval_document_id(nullable FK), folder_id(FK), title, disclosure_scope(PUBLIC/RESTRICTED), created_at
### ArchiveVersion
- id(PK), archive_document_id(FK), version_no, minio_object_key, created_at
- 최신 버전 포인터 유지.
### Folder / Tag / DocumentTag
- 폴더 트리 + 다대다 태그.

---

## 7. 알림 (NOTI)

### Notification
- id(PK), recipient_id(FK→Employee), type, ref_type(DOCUMENT/RESERVATION...), ref_id, message, is_read(bool), created_at
- 인덱스: (recipient_id, is_read).

---

## 8. 감사 로그 (공통)

### AuditLog
- id(PK), actor_id, action_type, target_type, target_id, summary, created_at
- append-only 지향. 무기한 보관.

---

## 핵심 제약 요약
- 유니크: emp_no, login_id, dept_code, loc_code, asset_no, holiday.date.
- 소프트 삭제: Employee/Department/Location/SignatureKey/개체비품(폐기) status로 관리.
- 결재선·서명은 스냅샷·키ID로 시점 고정.
- 예약 시간 겹침은 **EXCLUSION 제약(GiST+tstzrange)**으로 DB 차원 보장.
- 동시 수정 주요 엔티티는 `version` 컬럼(낙관적 잠금). 결재 상태 전이는 비관적 락(`02` AP-033).
- 스키마는 Flyway 마이그레이션으로 관리. 시각은 KST 기준.
