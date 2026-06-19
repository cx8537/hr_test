package com.example.hr.auth.dto;

import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.auth.entity.EmployeeRoleScope;
import jakarta.validation.constraints.NotNull;

/** 역할·범위 부여 DTO(FND-005/006). */
public final class RoleScopeDtos {

	private RoleScopeDtos() {
	}

	public record AssignRequest(
			@NotNull Long employeeId,
			@NotNull Role role,
			@NotNull ScopeType scopeType,
			Long scopeId) {
	}

	public record Response(Long id, Long employeeId, Role role, ScopeType scopeType, Long scopeId) {
		public static Response from(EmployeeRoleScope e) {
			return new Response(e.getId(), e.getEmployeeId(), e.getRole(), e.getScopeType(),
				e.getScopeId());
		}
	}
}
