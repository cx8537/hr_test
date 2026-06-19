package com.example.hr.org.repository;

import com.example.hr.org.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Optional<Employee> findByLoginId(String loginId);

	boolean existsByEmpNo(String empNo);

	boolean existsByLoginId(String loginId);
}
