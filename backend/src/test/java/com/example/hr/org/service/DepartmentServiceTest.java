package com.example.hr.org.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Department;
import com.example.hr.org.repository.DepartmentRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** FND-002: 부서 관리(3단계 강제·코드 중복 거부·소프트삭제). */
class DepartmentServiceTest {

	private DepartmentRepository departmentRepository;
	private com.example.hr.org.repository.EmployeeRepository employeeRepository;
	private DepartmentService departmentService;

	@BeforeEach
	void setUp() {
		departmentRepository = mock(DepartmentRepository.class);
		employeeRepository = mock(com.example.hr.org.repository.EmployeeRepository.class);
		departmentService = new DepartmentService(departmentRepository, employeeRepository);
	}

	@Test
	void FND002_루트부서_생성() {
		when(departmentRepository.existsByDeptCode("HQ")).thenReturn(false);
		when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

		assertThatCode(() -> departmentService.create("HQ", "본사", null)).doesNotThrowAnyException();
		verify(departmentRepository).save(any(Department.class));
	}

	@Test
	void FND002_코드중복_거부() {
		when(departmentRepository.existsByDeptCode("HQ")).thenReturn(true);

		assertThatThrownBy(() -> departmentService.create("HQ", "본사", null))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void FND002_AC1_4단계_생성거부() {
		Department level3Parent = new Department("D3", "파트", 2L, 3, EntityStatus.ACTIVE);
		when(departmentRepository.existsByDeptCode("D4")).thenReturn(false);
		when(departmentRepository.findById(99L)).thenReturn(Optional.of(level3Parent));

		assertThatThrownBy(() -> departmentService.create("D4", "하위", 99L))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void FND002_AC2_비활성화_소프트삭제() {
		Department dept = new Department("HQ", "본사", null, 1, EntityStatus.ACTIVE);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(employeeRepository.existsByDeptIdAndStatus(1L, EntityStatus.ACTIVE)).thenReturn(false);

		departmentService.deactivate(1L);

		org.assertj.core.api.Assertions.assertThat(dept.getStatus()).isEqualTo(EntityStatus.INACTIVE);
	}

	@Test
	void LIFEB2_AC1_소속직원_잔존시_비활성화_거부() {
		Department dept = new Department("HQ", "본사", null, 1, EntityStatus.ACTIVE);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(employeeRepository.existsByDeptIdAndStatus(1L, EntityStatus.ACTIVE)).thenReturn(true);

		assertThatThrownBy(() -> departmentService.deactivate(1L))
			.isInstanceOf(IllegalStateException.class);
		org.assertj.core.api.Assertions.assertThat(dept.getStatus()).isEqualTo(EntityStatus.ACTIVE);
	}

	@Test
	void LIFEB2_AC2_직원_없으면_비활성화_허용() {
		Department dept = new Department("HQ", "본사", null, 1, EntityStatus.ACTIVE);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(employeeRepository.existsByDeptIdAndStatus(1L, EntityStatus.ACTIVE)).thenReturn(false);

		departmentService.deactivate(1L);

		org.assertj.core.api.Assertions.assertThat(dept.getStatus()).isEqualTo(EntityStatus.INACTIVE);
	}
}
