package com.example.hr.auth;

import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.auth.dto.RoleScopeDtos.AssignRequest;
import com.example.hr.auth.dto.RoleScopeDtos.Response;
import com.example.hr.auth.service.RoleScopeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 역할·범위 부여/회수 API(FND-005/006). 시스템관리자 전용. */
@RestController
@RequestMapping("/api/admin/role-scopes")
public class RoleScopeController {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	private final RoleScopeService roleScopeService;
	private final AuthorizationService authorizationService;

	public RoleScopeController(RoleScopeService roleScopeService,
			AuthorizationService authorizationService) {
		this.roleScopeService = roleScopeService;
		this.authorizationService = authorizationService;
	}

	@PostMapping
	public Response assign(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody AssignRequest request) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return Response.from(roleScopeService.assign(
			request.employeeId(), request.role(), request.scopeType(), request.scopeId()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> revoke(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		roleScopeService.revoke(id);
		return ResponseEntity.noContent().build();
	}
}
