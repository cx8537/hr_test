package com.example.hr.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.auth.entity.EmployeeRoleScope;
import com.example.hr.auth.repository.EmployeeRoleScopeRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/** FND-006/010: RBAC API 강제(역할·범위 매 요청 판정). */
class AuthorizationServiceTest {

	private EmployeeRoleScopeRepository roleScopeRepository;
	private AuthorizationService authorizationService;

	@BeforeEach
	void setUp() {
		roleScopeRepository = mock(EmployeeRoleScopeRepository.class);
		authorizationService = new AuthorizationService(roleScopeRepository);
	}

	@Test
	void FND006_AC1_부서관리자_지정범위_허용() {
		when(roleScopeRepository.findByEmployeeId(1L)).thenReturn(
			List.of(new EmployeeRoleScope(1L, Role.DEPT_MANAGER, ScopeType.DEPT, 10L)));

		assertThat(authorizationService.isAllowed(1L,
			new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 10L))).isTrue();
	}

	@Test
	void FND006_AC1_부서관리자_범위외_거부() {
		when(roleScopeRepository.findByEmployeeId(1L)).thenReturn(
			List.of(new EmployeeRoleScope(1L, Role.DEPT_MANAGER, ScopeType.DEPT, 10L)));

		assertThat(authorizationService.isAllowed(1L,
			new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 99L))).isFalse();
	}

	@Test
	void FND006_시스템관리자_전역허용() {
		when(roleScopeRepository.findByEmployeeId(9L)).thenReturn(
			List.of(new EmployeeRoleScope(9L, Role.SYS_ADMIN, ScopeType.NONE, null)));

		assertThat(authorizationService.isAllowed(9L,
			new AccessRequest(Role.LOCATION_MANAGER, ScopeType.LOCATION, 3L))).isTrue();
	}

	@Test
	void FND010_AC2_범위밖_checkAllowed_AccessDenied() {
		when(roleScopeRepository.findByEmployeeId(2L)).thenReturn(List.of());

		assertThatThrownBy(() -> authorizationService.checkAllowed(2L,
			new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 10L)))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void FND010_허용시_checkAllowed_예외없음() {
		when(roleScopeRepository.findByEmployeeId(1L)).thenReturn(
			List.of(new EmployeeRoleScope(1L, Role.SYS_ADMIN, ScopeType.NONE, null)));

		assertThatCode(() -> authorizationService.checkAllowed(1L,
			new AccessRequest(Role.DEPT_MANAGER, ScopeType.DEPT, 1L))).doesNotThrowAnyException();
	}
}
