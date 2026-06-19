package com.example.hr.auth;

import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.RbacPolicy;
import com.example.hr.auth.domain.RoleAssignment;
import com.example.hr.auth.repository.EmployeeRoleScopeRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RBAC API 강제(FND-006/010). 인증된 사용자의 역할·범위를 매 요청 DB 조회해 판정한다.
 * 역할을 토큰에 고정하지 않으므로 권한 부여/회수가 즉시 반영된다(FND-006 AC4).
 */
@Service
public class AuthorizationService {

	private final EmployeeRoleScopeRepository roleScopeRepository;

	public AuthorizationService(EmployeeRoleScopeRepository roleScopeRepository) {
		this.roleScopeRepository = roleScopeRepository;
	}

	@Transactional(readOnly = true)
	public boolean isAllowed(Long employeeId, AccessRequest request) {
		List<RoleAssignment> assignments = roleScopeRepository.findByEmployeeId(employeeId).stream()
			.map(e -> new RoleAssignment(e.getRole(), e.getScopeType(), e.getScopeId()))
			.toList();
		return RbacPolicy.isAllowed(assignments, request);
	}

	/** 권한이 없으면 AccessDeniedException(→ 403). */
	public void checkAllowed(Long employeeId, AccessRequest request) {
		if (!isAllowed(employeeId, request)) {
			throw new AccessDeniedException("해당 작업에 대한 권한이 없습니다.");
		}
	}
}
