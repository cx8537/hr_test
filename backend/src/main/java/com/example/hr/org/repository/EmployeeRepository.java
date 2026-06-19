package com.example.hr.org.repository;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Optional<Employee> findByLoginId(String loginId);

	boolean existsByEmpNo(String empNo);

	boolean existsByLoginId(String loginId);

	/** 부서 소속 직원 잔존 여부(부서 비활성화 가능 판정 LIFE-B2). */
	boolean existsByDeptIdAndStatus(Long deptId, EntityStatus status);
}
