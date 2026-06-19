package com.example.hr.org.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.DepartmentRepository;
import com.example.hr.org.repository.EmployeeRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/** FND-001: 임직원 관리(중복 거부·비밀번호 정책·소프트삭제·리셋). */
class EmployeeServiceTest {

	private EmployeeRepository employeeRepository;
	private DepartmentRepository departmentRepository;
	private PasswordEncoder passwordEncoder;
	private EmployeeService employeeService;

	@BeforeEach
	void setUp() {
		employeeRepository = mock(EmployeeRepository.class);
		departmentRepository = mock(DepartmentRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		employeeService = new EmployeeService(employeeRepository, departmentRepository, passwordEncoder);
	}

	@Test
	void FND001_정상등록_비밀번호해시() {
		when(employeeRepository.existsByEmpNo("E001")).thenReturn(false);
		when(employeeRepository.existsByLoginId("hong")).thenReturn(false);
		when(departmentRepository.existsById(1L)).thenReturn(true);
		when(passwordEncoder.encode("abcd1234")).thenReturn("$hashed");
		when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

		employeeService.register("E001", "hong", "abcd1234", "홍길동", 1L, "사원", null, null);

		verify(passwordEncoder).encode("abcd1234");
		verify(employeeRepository).save(any(Employee.class));
	}

	@Test
	void FND003_AC3_비밀번호정책위반_거부() {
		assertThatThrownBy(() ->
			employeeService.register("E001", "hong", "short", "홍길동", 1L, null, null, null))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void FND001_AC1_사번중복_거부() {
		when(employeeRepository.existsByEmpNo("E001")).thenReturn(true);

		assertThatThrownBy(() ->
			employeeService.register("E001", "hong", "abcd1234", "홍길동", 1L, null, null, null))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void FND001_AC1_로그인ID중복_거부() {
		when(employeeRepository.existsByEmpNo("E002")).thenReturn(false);
		when(employeeRepository.existsByLoginId("hong")).thenReturn(true);

		assertThatThrownBy(() ->
			employeeService.register("E002", "hong", "abcd1234", "홍길동", 1L, null, null, null))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void FND001_AC4_비활성화_소프트삭제() {
		Employee emp = new Employee("E001", "hong", "$h", "홍길동", 1L, null, null, null,
			EntityStatus.ACTIVE, 0, false);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

		employeeService.deactivate(1L);

		assertThat(emp.getStatus()).isEqualTo(EntityStatus.INACTIVE);
	}

	@Test
	void FND_비밀번호리셋_변경강제() {
		Employee emp = new Employee("E001", "hong", "$old", "홍길동", 1L, null, null, null,
			EntityStatus.ACTIVE, 0, false);
		when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
		when(passwordEncoder.encode(anyString())).thenReturn("$new");

		employeeService.resetPassword(1L, "temp1234");

		assertThat(emp.isMustChangePassword()).isTrue();
		assertThat(emp.getPasswordHash()).isEqualTo("$new");
	}
}
