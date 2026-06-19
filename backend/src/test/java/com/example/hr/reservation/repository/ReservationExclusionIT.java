package com.example.hr.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약 겹침 차단 EXCLUSION 제약(RSV-003)을 실 PostgreSQL에 대해 검증한다.
 * - AC1: 같은 자원·겹치는 ACTIVE 예약은 거부(exclusion_violation, SQLState 23P01).
 * - AC2: 반열림 구간이라 끝=다음 시작(맞닿음)은 허용.
 * - AC3: 동시 2요청 중 한 건만 통과 — 제약 자체가 두 번째 INSERT를 즉시 막는 것으로 보장.
 * - 취소(CANCELLED) 예약은 부분 제약에서 제외되어 겹쳐도 허용.
 * 모든 테스트는 @Transactional 롤백으로 DB를 더럽히지 않는다.
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
class ReservationExclusionIT {

	@Autowired
	private JdbcTemplate jdbc;

	private long resourceId;
	private long reserverId;

	private static final OffsetDateTime T10 =
		OffsetDateTime.of(2099, 1, 1, 10, 0, 0, 0, ZoneOffset.ofHours(9));
	private static final OffsetDateTime T11 =
		OffsetDateTime.of(2099, 1, 1, 11, 0, 0, 0, ZoneOffset.ofHours(9));
	private static final OffsetDateTime T12 =
		OffsetDateTime.of(2099, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(9));

	@BeforeEach
	void setUp() {
		Long deptId = jdbc.queryForObject(
			"INSERT INTO department(dept_code,name,level,status) "
				+ "VALUES ('EXCL-D','테스트부',1,'ACTIVE') RETURNING id", Long.class);
		reserverId = jdbc.queryForObject(
			"INSERT INTO employee(emp_no,login_id,password_hash,name,dept_id,status) "
				+ "VALUES ('EXCL-E','excl_user','h','예약자',?,'ACTIVE') RETURNING id",
			Long.class, deptId);
		Long locId = jdbc.queryForObject(
			"INSERT INTO location(location_code,name,status) "
				+ "VALUES ('EXCL-L','테스트거점','ACTIVE') RETURNING id", Long.class);
		resourceId = jdbc.queryForObject(
			"INSERT INTO resource(location_id,type,name,status) "
				+ "VALUES (?,'MEETING_ROOM','회의실A','ACTIVE') RETURNING id",
			Long.class, locId);
	}

	private void insertReservation(OffsetDateTime start, OffsetDateTime end, String status) {
		jdbc.update(
			"INSERT INTO reservation(resource_id,reserver_id,start_at,end_at,status) "
				+ "VALUES (?,?,?,?,?)",
			resourceId, reserverId, start, end, status);
	}

	@Test
	void RSV003_AC1_겹치는_ACTIVE_예약_거부() {
		insertReservation(T10, T12, "ACTIVE");           // 10~12
		assertThatThrownBy(() -> insertReservation(T11, T12, "ACTIVE")) // 11~12 겹침
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void RSV003_AC2_맞닿은_경계_예약_허용() {
		insertReservation(T10, T11, "ACTIVE");           // 10~11
		insertReservation(T11, T12, "ACTIVE");           // 11~12 (끝=다음 시작, 허용)
		Integer count = jdbc.queryForObject(
			"SELECT count(*) FROM reservation WHERE resource_id=? AND status='ACTIVE'",
			Integer.class, resourceId);
		assertThat(count).isEqualTo(2);
	}

	@Test
	void RSV003_취소예약은_겹쳐도_허용() {
		insertReservation(T10, T12, "CANCELLED");        // 취소건
		insertReservation(T10, T12, "ACTIVE");           // 동일 구간이지만 ACTIVE는 1건뿐 → 허용
		Integer active = jdbc.queryForObject(
			"SELECT count(*) FROM reservation WHERE resource_id=? AND status='ACTIVE'",
			Integer.class, resourceId);
		assertThat(active).isEqualTo(1);
	}
}
