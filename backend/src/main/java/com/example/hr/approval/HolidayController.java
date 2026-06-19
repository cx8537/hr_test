package com.example.hr.approval;

import com.example.hr.approval.dto.HolidayDtos.Response;
import com.example.hr.approval.dto.HolidayDtos.SaveRequest;
import com.example.hr.approval.service.HolidayService;
import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 공휴일 관리 API(AP-043). 시스템관리자 전용 CRUD. */
@RestController
@RequestMapping("/api/admin/holidays")
public class HolidayController {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	private final HolidayService holidayService;
	private final AuthorizationService authorizationService;

	public HolidayController(HolidayService holidayService,
			AuthorizationService authorizationService) {
		this.holidayService = holidayService;
		this.authorizationService = authorizationService;
	}

	@GetMapping
	public List<Response> list(@AuthenticationPrincipal Long actorId,
			@RequestParam int year) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return holidayService.findByYear(year).stream().map(Response::from).toList();
	}

	@PostMapping
	public Response create(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody SaveRequest request) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return Response.from(holidayService.create(request.date(), request.name()));
	}

	@PutMapping("/{id}")
	public Response update(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@Valid @RequestBody SaveRequest request) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return Response.from(holidayService.update(id, request.date(), request.name()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		holidayService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
