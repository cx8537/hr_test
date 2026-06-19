package com.example.hr.auth.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.auth.entity.EmployeeRoleScope;
import com.example.hr.auth.repository.EmployeeRoleScopeRepository;
import com.example.hr.org.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** FND-005/006: 역할·범위 부여/회수. */
class RoleScopeServiceTest {

	private EmployeeRoleScopeRepository roleScopeRepository;
	private EmployeeRepository employeeRepository;
	private RoleScopeService roleScopeService;

	@BeforeEach
	void setUp() {
		roleScopeRepository = mock(EmployeeRoleScopeRepository.class);
		employeeRepository = mock(EmployeeRepository.class);
		roleScopeService = new RoleScopeService(roleScopeRepository, employeeRepository);
	}

	@Test
	void FND006_범위역할_부여() {
		when(employeeRepository.existsById(1L)).thenReturn(true);
		when(roleScopeRepository.save(any(EmployeeRoleScope.class)))
			.thenAnswer(inv -> inv.getArgument(0));

		assertThatCode(() -> roleScopeService.assign(1L, Role.DEPT_MANAGER, ScopeType.DEPT, 10L))
			.doesNotThrowAnyException();
		verify(roleScopeRepository).save(any(EmployeeRoleScope.class));
	}

	@Test
	void FND006_범위역할_scopeId누락_거부() {
		when(employeeRepository.existsById(1L)).thenReturn(true);

		assertThatThrownBy(() -> roleScopeService.assign(1L, Role.DEPT_MANAGER, ScopeType.DEPT, null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void FND005_없는임직원_부여거부() {
		when(employeeRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> roleScopeService.assign(99L, Role.GENERAL, ScopeType.NONE, null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void FND006_역할회수() {
		when(roleScopeRepository.existsById(5L)).thenReturn(true);

		roleScopeService.revoke(5L);

		verify(roleScopeRepository).deleteById(5L);
	}
}
