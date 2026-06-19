package com.example.hr.org;

import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.org.dto.EmployeeDtos.CreateRequest;
import com.example.hr.org.dto.EmployeeDtos.ResetPasswordRequest;
import com.example.hr.org.dto.EmployeeDtos.Response;
import com.example.hr.org.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 임직원 관리 API(FND-001). 시스템관리자 전용. */
@RestController
@RequestMapping("/api/admin/employees")
public class EmployeeController {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	private final EmployeeService employeeService;
	private final AuthorizationService authorizationService;

	public EmployeeController(EmployeeService employeeService,
			AuthorizationService authorizationService) {
		this.employeeService = employeeService;
		this.authorizationService = authorizationService;
	}

	@PostMapping
	public Response register(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody CreateRequest request) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return Response.from(employeeService.register(
			request.empNo(), request.loginId(), request.password(), request.name(),
			request.deptId(), request.position(), request.email(), request.phone()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deactivate(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		employeeService.deactivate(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/password-reset")
	public ResponseEntity<Void> resetPassword(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		employeeService.resetPassword(id, request.tempPassword());
		return ResponseEntity.noContent().build();
	}
}
