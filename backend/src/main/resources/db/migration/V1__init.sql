-- V1 베이스라인: 공통 인프라(확장 · 트리거 함수 · 규약).
-- 실제 모듈 테이블은 후속 마이그레이션(V2~, 모듈별)에서 추가한다.

-- 예약 시간 겹침 EXCLUSION 제약(GiST + tstzrange + resource_id =)에 필요(RSV-003).
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- updated_at 자동 갱신 트리거 함수. 후속 테이블에서 재사용한다:
--   CREATE TRIGGER trg_set_updated_at BEFORE UPDATE ON <table>
--     FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================================
-- 공통 컬럼 규약 (모든 후속 테이블이 따른다)
-- ---------------------------------------------------------------------
-- 시각: 모든 시각 컬럼은 TIMESTAMPTZ, 타임존 KST(Asia/Seoul) 기준.
--   created_at TIMESTAMPTZ NOT NULL DEFAULT now()
--   updated_at TIMESTAMPTZ NOT NULL DEFAULT now()  (set_updated_at 트리거로 갱신)
-- 낙관적 잠금: 동시 수정 가능 엔티티(Location/AssetItem/Resource 등)는
--   version BIGINT NOT NULL DEFAULT 0  (JPA @Version 매핑)
--
-- 상태값은 PostgreSQL ENUM 타입 대신 VARCHAR + CHECK 로 강제한다
-- (JPA @Enumerated(STRING) 매핑 친화). 각 테이블 마이그레이션에서 CHECK 부여:
--   Employee.status / Department.status / Location.status / Resource.status
--                                              : 'ACTIVE','INACTIVE'
--   SignatureKey.status                        : 'ACTIVE','REVOKED'
--   IndividualAsset.state                      : 'IN_USE','STORED','REPAIR','DISCARDED'
--   AssetItem.manage_type                      : 'INDIVIDUAL','QUANTITY'
--   StockTransaction.type                      : 'IN','OUT'
--   ApprovalDocument.status                    : 'DRAFT','IN_PROGRESS','ON_HOLD','APPROVED','REJECTED','WITHDRAWN'
--   ApprovalDocument.form_type                 : 'EXPENSE','LEAVE','ASSET_REQ','GENERAL'
--   ApprovalLineSnapshot.step_type             : 'SEQUENTIAL','PARALLEL','CONSENT'
--   ApprovalLineSnapshot.state                 : 'PENDING','APPROVED','REJECTED','CONSENT_REJECTED','SKIPPED'
--   FormTypeConfig.disclosure_scope / ArchiveDocument.disclosure_scope
--                                              : 'PUBLIC','RESTRICTED'
--   Role.code                                  : 'GENERAL','DEPT_MANAGER','LOCATION_MANAGER','ASSET_MANAGER','SYS_ADMIN'
--   EmployeeRoleScope.scope_type               : 'NONE','DEPT','LOCATION'
--   Resource.type                              : 'VEHICLE','MEETING_ROOM'
--   Reservation.status                         : 'CONFIRMED','CANCELLED'
--   ArchiveDocument.source_type                : 'APPROVAL','UPLOAD'
--
-- 유니크: emp_no, login_id, dept_code, loc_code, asset_no, holiday.date.
-- 소프트 삭제: 물리 삭제 대신 status 전이(ACTIVE→INACTIVE/REVOKED).
-- =====================================================================
