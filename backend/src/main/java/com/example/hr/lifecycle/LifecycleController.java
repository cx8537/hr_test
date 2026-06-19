package com.example.hr.lifecycle;

import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.lifecycle.service.LifecycleService;
import com.example.hr.lifecycle.service.LifecycleService.ResignResult;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 라이프사이클 API(LIFE-A). 퇴사 처리는 시스템관리자 전용. 응답에 진행 중 결재 교체 안내 목록 포함(LIFE-A2).
 */
@RestController
@RequestMapping("/api/lifecycle")
public class LifecycleController {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	private final LifecycleService lifecycleService;
	private final AuthorizationService authorizationService;

	public LifecycleController(LifecycleService lifecycleService,
			AuthorizationService authorizationService) {
		this.lifecycleService = lifecycleService;
		this.authorizationService = authorizationService;
	}

	/** 퇴사 처리(LIFE-A1~A3/A5). 결과로 결재선 교체가 필요한 진행 중 문서 목록을 반환한다(A2 안내). */
	@PostMapping("/employees/{id}/resign")
	public ResignResult resign(@AuthenticationPrincipal Long actorId, @PathVariable Long id) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN);
		return lifecycleService.resign(id);
	}
}
