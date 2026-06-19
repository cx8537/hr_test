package com.example.hr.org.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.config.ClockConfig;
import com.example.hr.config.JpaConfig;
import com.example.hr.org.entity.Department;
import com.example.hr.org.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * FND 영속성 통합 테스트(로컬 PostgreSQL `hr` DB).
 * local 프로파일로 application-local.yml의 실 자격증명을 사용하고,
 * JPA Auditing(OffsetDateTime DateTimeProvider) 설정을 import 해 created_at 자동 채움을 검증한다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("local")
@Import({JpaConfig.class, ClockConfig.class})
class EmployeeRepositoryIT {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	private Employee newEmployee(String empNo, String loginId, Long deptId) {
		return new Employee(empNo, loginId, "$2a$10$hash", "홍길동", deptId,
			"사원", null, null, EntityStatus.ACTIVE, 0, true);
	}

	@Test
	void FND001_임직원_저장_조회() {
		Department dept = departmentRepository.save(
			new Department("D001", "영업부", null, 1, EntityStatus.ACTIVE));
		Employee saved = employeeRepository.save(newEmployee("E001", "hong", dept.getId()));

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(employeeRepository.findByLoginId("hong")).isPresent();
	}

	@Test
	void FND001_AC1_로그인ID_중복_거부() {
		Department dept = departmentRepository.save(
			new Department("D002", "총무부", null, 1, EntityStatus.ACTIVE));
		employeeRepository.saveAndFlush(newEmployee("E010", "dup", dept.getId()));

		assertThatThrownBy(() ->
			employeeRepository.saveAndFlush(newEmployee("E011", "dup", dept.getId())))
			.isInstanceOf(Exception.class);
	}
}
