package com.example.hr.auth.domain;

import java.util.List;
import java.util.Objects;

/**
 * RBAC 권한 판정(FND-006): (사용자 역할·범위 목록, 요청 대상) → 허용/거부.
 * 순수 함수로 표 기반 단위 테스트 대상(09 §4).
 *
 * 규칙:
 * - 시스템관리자(SYS_ADMIN) 보유 시 전역 허용.
 * - 그 외에는 요구 역할을 동일 범위(유형+ID 일치)로 보유해야 허용. 여러 범위 중 하나라도 매칭되면 허용(AC3).
 */
public final class RbacPolicy {

	private RbacPolicy() {
	}

	public static boolean isAllowed(List<RoleAssignment> assignments, AccessRequest request) {
		if (assignments == null || assignments.isEmpty()) {
			return false;
		}
		boolean isSysAdmin = assignments.stream().anyMatch(a -> a.role() == Role.SYS_ADMIN);
		if (isSysAdmin) {
			return true;
		}
		return assignments.stream().anyMatch(a -> matches(a, request));
	}

	private static boolean matches(RoleAssignment assignment, AccessRequest request) {
		return assignment.role() == request.requiredRole()
			&& assignment.scopeType() == request.targetScopeType()
			&& Objects.equals(assignment.scopeId(), request.targetScopeId());
	}
}
