package com.example.hr.org.service;

import com.example.hr.auth.domain.PasswordPolicy;
import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.DepartmentRepository;
import com.example.hr.org.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 임직원 관리(FND-001). 사번·로그인ID 중복 거부, 비밀번호 정책 검증+BCrypt,
 * 비활성화=소프트삭제, 관리자 비밀번호 리셋(임시발급+변경 강제).
 */
@Service
public class EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final PasswordEncoder passwordEncoder;

	public EmployeeService(EmployeeRepository employeeRepository,
			DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public Employee register(String empNo, String loginId, String password, String name,
			Long deptId, String position, String email, String phone) {
		if (!PasswordPolicy.isValid(password)) {
			throw new IllegalArgumentException("비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다."); // FND-003 AC3
		}
		if (employeeRepository.existsByEmpNo(empNo)) {
			throw new IllegalStateException("이미 존재하는 사번입니다: " + empNo); // FND-001 AC1
		}
		if (employeeRepository.existsByLoginId(loginId)) {
			throw new IllegalStateException("이미 존재하는 로그인 ID입니다: " + loginId); // FND-001 AC1
		}
		if (!departmentRepository.existsById(deptId)) {
			throw new IllegalArgumentException("소속 부서를 찾을 수 없습니다."); // FND-001 AC2
		}
		// 신규 계정은 최초 로그인 시 비밀번호 변경을 강제한다.
		return employeeRepository.save(new Employee(
			empNo, loginId, passwordEncoder.encode(password), name, deptId,
			position, email, phone, EntityStatus.ACTIVE, 0, true));
	}

	@Transactional
	public void deactivate(Long id) {
		Employee employee = employeeRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("임직원을 찾을 수 없습니다."));
		employee.deactivate();
	}

	@Transactional
	public void resetPassword(Long id, String tempPassword) {
		if (!PasswordPolicy.isValid(tempPassword)) {
			throw new IllegalArgumentException("비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.");
		}
		Employee employee = employeeRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("임직원을 찾을 수 없습니다."));
		employee.resetPassword(passwordEncoder.encode(tempPassword));
	}
}
