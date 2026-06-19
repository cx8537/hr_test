package com.example.hr.org.repository;

import com.example.hr.org.entity.Department;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

	Optional<Department> findByDeptCode(String deptCode);

	boolean existsByDeptCode(String deptCode);
}
