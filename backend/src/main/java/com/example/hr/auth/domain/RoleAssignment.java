package com.example.hr.auth.domain;

/**
 * 사용자에게 부여된 역할·범위 쌍(FND-006). 한 사용자가 여러 개 보유 가능(다대다).
 * scopeId는 scopeType이 DEPT/LOCATION일 때 대상 ID, NONE이면 null.
 */
public record RoleAssignment(Role role, ScopeType scopeType, Long scopeId) {
}
