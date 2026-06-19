package com.example.hr.org;

import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.org.dto.DepartmentDtos.CreateRequest;
import com.example.hr.org.dto.DepartmentDtos.Response;
import com.example.hr.org.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 부서 관리 API(FND-002). 시스템관리자 전용. */
@RestController
@RequestMapping("/api/admin/departments")
public class DepartmentController {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	private final DepartmentService departmentService;
	private final AuthorizationService authorizationService;

	public DepartmentController(DepartmentService departmentService,
			AuthorizationService authorizationService) {
		this.departmentService = departmentService;
		this.authorizationService = authorizationService;
	}

	@PostMapping
	public Response create(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody CreateRequest request) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return Response.from(
			departmentService.create(request.deptCode(), request.name(), request.parentId()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivate(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		departmentService.deactivate(id);
		return ResponseEntity.noContent().build();
	}
}
