package com.example.hr.auth.repository;

import com.example.hr.auth.domain.Role;
import com.example.hr.auth.entity.EmployeeRoleScope;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRoleScopeRepository extends JpaRepository<EmployeeRoleScope, Long> {

	List<EmployeeRoleScope> findByEmployeeId(Long employeeId);

	/** 특정 역할 보유자(LIFE-A5 시스템관리자 수 집계 등). */
	List<EmployeeRoleScope> findByRole(Role role);
}
