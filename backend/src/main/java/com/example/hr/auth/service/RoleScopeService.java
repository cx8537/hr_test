package com.example.hr.auth.service;

import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.auth.entity.EmployeeRoleScope;
import com.example.hr.auth.repository.EmployeeRoleScopeRepository;
import com.example.hr.org.repository.EmployeeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 역할·범위 부여/회수(FND-005/006). 부여/회수는 즉시 반영(토큰 비의존). */
@Service
public class RoleScopeService {

	private final EmployeeRoleScopeRepository roleScopeRepository;
	private final EmployeeRepository employeeRepository;

	public RoleScopeService(EmployeeRoleScopeRepository roleScopeRepository,
			EmployeeRepository employeeRepository) {
		this.roleScopeRepository = roleScopeRepository;
		this.employeeRepository = employeeRepository;
	}

	@Transactional
	public EmployeeRoleScope assign(Long employeeId, Role role, ScopeType scopeType, Long scopeId) {
		if (!employeeRepository.existsById(employeeId)) {
			throw new IllegalArgumentException("임직원을 찾을 수 없습니다.");
		}
		// 범위형 역할(DEPT/LOCATION)은 scopeId 필수, 전역/일반 역할은 scopeId 없음.
		if (scopeType != ScopeType.NONE && scopeId == null) {
			throw new IllegalArgumentException("해당 역할에는 적용 범위(scopeId)가 필요합니다.");
		}
		if (scopeType == ScopeType.NONE && scopeId != null) {
			throw new IllegalArgumentException("범위 없는 역할에는 scopeId를 지정할 수 없습니다.");
		}
		return roleScopeRepository.save(new EmployeeRoleScope(employeeId, role, scopeType, scopeId));
	}

	@Transactional
	public void revoke(Long roleScopeId) {
		if (!roleScopeRepository.existsById(roleScopeId)) {
			throw new IllegalArgumentException("역할 부여 내역을 찾을 수 없습니다.");
		}
		roleScopeRepository.deleteById(roleScopeId);
	}

	@Transactional(readOnly = true)
	public List<EmployeeRoleScope> listByEmployee(Long employeeId) {
		return roleScopeRepository.findByEmployeeId(employeeId);
	}
}
