package com.example.hr.org.service;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.domain.DepartmentTree;
import com.example.hr.org.entity.Department;
import com.example.hr.org.repository.DepartmentRepository;
import com.example.hr.org.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 부서 관리(FND-002). 3단계 트리 강제, 코드 중복 거부, 비활성화=소프트삭제(직원 잔존 시 거부 LIFE-B2). */
@Service
public class DepartmentService {

	private final DepartmentRepository departmentRepository;
	private final EmployeeRepository employeeRepository;

	public DepartmentService(DepartmentRepository departmentRepository,
			EmployeeRepository employeeRepository) {
		this.departmentRepository = departmentRepository;
		this.employeeRepository = employeeRepository;
	}

	@Transactional
	public Department create(String deptCode, String name, Long parentId) {
		if (departmentRepository.existsByDeptCode(deptCode)) {
			throw new IllegalStateException("이미 존재하는 부서코드입니다: " + deptCode);
		}
		Integer parentLevel = null;
		if (parentId != null) {
			Department parent = departmentRepository.findById(parentId)
				.orElseThrow(() -> new IllegalArgumentException("상위 부서를 찾을 수 없습니다."));
			parentLevel = parent.getLevel();
		}
		if (!DepartmentTree.canAttach(parentLevel)) {
			throw new IllegalArgumentException("부서는 최대 3단계까지만 생성할 수 있습니다."); // FND-002 AC1
		}
		int level = DepartmentTree.childLevel(parentLevel);
		return departmentRepository.save(
			new Department(deptCode, name, parentId, level, EntityStatus.ACTIVE));
	}

	@Transactional
	public void deactivate(Long id) {
		Department dept = departmentRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("부서를 찾을 수 없습니다."));
		if (employeeRepository.existsByDeptIdAndStatus(id, EntityStatus.ACTIVE)) {
			throw new IllegalStateException("소속 직원이 있어 부서를 비활성화할 수 없습니다."); // LIFE-B2 AC1
		}
		dept.deactivate();
	}
}
