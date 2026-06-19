package com.example.hr.auth.domain;

/**
 * 관리 액션 접근 요청. 어떤 역할이 요구되고, 어떤 범위 대상인지를 표현한다(FND-006).
 * targetScopeId는 targetScopeType이 DEPT/LOCATION일 때 대상 ID.
 */
public record AccessRequest(Role requiredRole, ScopeType targetScopeType, Long targetScopeId) {
}
