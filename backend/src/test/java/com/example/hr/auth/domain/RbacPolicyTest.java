package com.example.hr.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** FND-006: RBAC 권한 판정 (역할×범위 → 허용/거부). */
class RbacPolicyTest {

	@Test
	void FND006_시스템관리자_전역허용() {
		var user = List.of(new RoleAssignment(Role.SYS_ADMIN, ScopeType.NONE, null));
		assertThat(RbacPolicy.isAllowed(user, new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 5L)))
			.isTrue();
	}

	@Test
	void FND006_AC1_부서관리자_지정범위_허용() {
		var user = List.of(new RoleAssignment(Role.DEPT_MANAGER, ScopeType.DEPT, 10L));
		assertThat(RbacPolicy.isAllowed(user, new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 10L)))
			.isTrue();
	}

	@Test
	void FND006_AC1_부서관리자_범위외_거부() {
		var user = List.of(new RoleAssignment(Role.DEPT_MANAGER, ScopeType.DEPT, 10L));
		assertThat(RbacPolicy.isAllowed(user, new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 99L)))
			.isFalse();
	}

	@Test
	void FND006_AC2_거점관리자_범위외_거부() {
		var user = List.of(new RoleAssignment(Role.LOCATION_MANAGER, ScopeType.LOCATION, 1L));
		assertThat(
			RbacPolicy.isAllowed(user, new AccessRequest(Role.LOCATION_MANAGER, ScopeType.LOCATION, 2L)))
			.isFalse();
	}

	@Test
	void FND006_AC3_다중범위_하나매칭_허용() {
		var user = List.of(
			new RoleAssignment(Role.DEPT_MANAGER, ScopeType.DEPT, 10L),
			new RoleAssignment(Role.DEPT_MANAGER, ScopeType.DEPT, 20L));
		assertThat(RbacPolicy.isAllowed(user, new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 20L)))
			.isTrue();
	}

	@Test
	void FND006_요구역할_미보유_거부() {
		var user = List.of(new RoleAssignment(Role.GENERAL, ScopeType.NONE, null));
		assertThat(RbacPolicy.isAllowed(user, new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 10L)))
			.isFalse();
	}

	@Test
	void FND006_다른범위유형_거부() {
		// 거점관리자(LOCATION) 권한으로 부서(DEPT) 관리 요청은 거부
		var user = List.of(new RoleAssignment(Role.LOCATION_MANAGER, ScopeType.LOCATION, 10L));
		assertThat(RbacPolicy.isAllowed(user, new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 10L)))
			.isFalse();
	}
}
