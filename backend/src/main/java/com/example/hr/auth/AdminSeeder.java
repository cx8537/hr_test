package com.example.hr.auth;

import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.auth.entity.EmployeeRoleScope;
import com.example.hr.auth.repository.EmployeeRoleScopeRepository;
import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Department;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.DepartmentRepository;
import com.example.hr.org.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 최초 시스템관리자 시드(FND-001 AC5). 등록 권한의 닭-달걀 문제 해소를 위해
 * 기동 시 관리자 계정이 없으면 기본 부서와 함께 생성한다(must_change_password=true).
 * 초기 비밀번호는 환경변수(BOOTSTRAP_ADMIN_*)로 주입하며 최초 로그인 시 변경을 강제한다.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

	private static final String ROOT_DEPT_CODE = "ROOT";

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final EmployeeRoleScopeRepository roleScopeRepository;
	private final PasswordEncoder passwordEncoder;
	private final String adminLoginId;
	private final String adminInitPassword;

	public AdminSeeder(EmployeeRepository employeeRepository,
			DepartmentRepository departmentRepository,
			EmployeeRoleScopeRepository roleScopeRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap-admin.login-id}") String adminLoginId,
			@Value("${app.bootstrap-admin.init-password}") String adminInitPassword) {
		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
		this.roleScopeRepository = roleScopeRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminLoginId = adminLoginId;
		this.adminInitPassword = adminInitPassword;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (employeeRepository.existsByLoginId(adminLoginId)) {
			return;
		}
		if (adminInitPassword == null || adminInitPassword.isBlank()) {
			throw new IllegalStateException(
				"BOOTSTRAP_ADMIN_INIT_PASSWORD 가 비어 있어 최초 관리자를 시드할 수 없습니다.");
		}

		Department rootDept = departmentRepository.findByDeptCode(ROOT_DEPT_CODE)
			.orElseGet(() -> departmentRepository.save(
				new Department(ROOT_DEPT_CODE, "본사", null, 1, EntityStatus.ACTIVE)));

		Employee admin = employeeRepository.save(new Employee(
			"ADMIN-0001", adminLoginId, passwordEncoder.encode(adminInitPassword),
			"시스템관리자", rootDept.getId(), null, null, null,
			EntityStatus.ACTIVE, 0, true));

		roleScopeRepository.save(
			new EmployeeRoleScope(admin.getId(), Role.SYS_ADMIN, ScopeType.NONE, null));
	}
}
