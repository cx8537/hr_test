package com.example.hr.auth.entity;

import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 임직원에게 부여된 역할·범위(FND-006). 한 직원이 여러 개 보유 가능(다대다 + 범위).
 * 모듈 결합 최소화를 위해 employeeId(Long)로 참조한다.
 */
@Entity
@Table(name = "employee_role_scope")
public class EmployeeRoleScope extends BaseEntity {

	@Column(name = "employee_id", nullable = false)
	private Long employeeId;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_code", nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(name = "scope_type", nullable = false)
	private ScopeType scopeType;

	@Column(name = "scope_id")
	private Long scopeId;

	protected EmployeeRoleScope() {
	}

	public EmployeeRoleScope(Long employeeId, Role role, ScopeType scopeType, Long scopeId) {
		this.employeeId = employeeId;
		this.role = role;
		this.scopeType = scopeType;
		this.scopeId = scopeId;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public Role getRole() {
		return role;
	}

	public ScopeType getScopeType() {
		return scopeType;
	}

	public Long getScopeId() {
		return scopeId;
	}
}
