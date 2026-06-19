package com.example.hr.auth.repository;

import com.example.hr.auth.entity.EmployeeRoleScope;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRoleScopeRepository extends JpaRepository<EmployeeRoleScope, Long> {

	List<EmployeeRoleScope> findByEmployeeId(Long employeeId);
}
