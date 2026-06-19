package com.example.hr.signature;

import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.signature.dto.SignatureKeyDtos.IssueRequest;
import com.example.hr.signature.dto.SignatureKeyDtos.Response;
import com.example.hr.signature.service.SignatureKeyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 결재 공개키 API(FND-007/009). 발급·재발급·조회는 본인, 폐기는 시스템관리자.
 * 서버는 공개키만 받아 저장하며 개인키는 전송받지 않는다(브라우저 1회 다운로드).
 */
@RestController
@RequestMapping("/api/signature-keys")
public class SignatureKeyController {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	private final SignatureKeyService signatureKeyService;
	private final AuthorizationService authorizationService;

	public SignatureKeyController(SignatureKeyService signatureKeyService,
			AuthorizationService authorizationService) {
		this.signatureKeyService = signatureKeyService;
		this.authorizationService = authorizationService;
	}

	@PostMapping
	public Response issue(@AuthenticationPrincipal Long employeeId,
			@Valid @RequestBody IssueRequest request) {
		return Response.from(signatureKeyService.issue(employeeId, request.publicKey()));
	}

	@PostMapping("/reissue")
	public Response reissue(@AuthenticationPrincipal Long employeeId,
			@Valid @RequestBody IssueRequest request) {
		return Response.from(signatureKeyService.reissue(employeeId, request.publicKey()));
	}

	@GetMapping("/me")
	public List<Response> myActiveKeys(@AuthenticationPrincipal Long employeeId) {
		return signatureKeyService.findActiveByEmployee(employeeId).stream()
			.map(Response::from)
			.toList();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> revoke(@AuthenticationPrincipal Long actorId, @PathVariable Long id) {
		authorizationService.checkAllowed(actorId, SYS_ADMIN); // 폐기는 시스템관리자
		signatureKeyService.revoke(id);
		return ResponseEntity.noContent().build();
	}
}
