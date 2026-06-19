package com.example.hr.reservation;

import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.reservation.dto.ReservationDtos.CancelRequest;
import com.example.hr.reservation.dto.ReservationDtos.CreateResourceRequest;
import com.example.hr.reservation.dto.ReservationDtos.ReservationResponse;
import com.example.hr.reservation.dto.ReservationDtos.ReserveRequest;
import com.example.hr.reservation.dto.ReservationDtos.ResourceResponse;
import com.example.hr.reservation.service.ReservationService;
import com.example.hr.reservation.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 예약·자원 API(RSV-002/003/005/007). 예약 생성·취소는 인증 사용자, 자원 관리·타인 취소는 거점관리자 RBAC.
 */
@RestController
@RequestMapping("/api")
public class ReservationController {

	private final ReservationService reservationService;
	private final ResourceService resourceService;
	private final AuthorizationService authorizationService;

	public ReservationController(ReservationService reservationService,
			ResourceService resourceService, AuthorizationService authorizationService) {
		this.reservationService = reservationService;
		this.resourceService = resourceService;
		this.authorizationService = authorizationService;
	}

	private void requireLocationManager(Long actorId, Long locationId) {
		authorizationService.checkAllowed(actorId,
			new AccessRequest(Role.LOCATION_MANAGER, ScopeType.LOCATION, locationId));
	}

	/** 자원 등록(RSV-001): 거점관리자(해당 거점). */
	@PostMapping("/resources")
	public ResourceResponse createResource(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody CreateResourceRequest request) {
		requireLocationManager(actorId, request.locationId());
		return ResourceResponse.from(
			resourceService.register(request.locationId(), request.type(), request.name()));
	}

	/** 자원 비활성화(RSV-007): 미래 예약 자동 취소. 거점관리자. */
	@PostMapping("/resources/{id}/deactivate")
	public ResponseEntity<Void> deactivateResource(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		requireLocationManager(actorId, resourceService.locationIdOfResource(id));
		resourceService.deactivate(id, actorId);
		return ResponseEntity.noContent().build();
	}

	/** 즉시 예약(RSV-002): 인증 사용자 누구나. 겹침은 서비스/제약이 차단(RSV-003). */
	@PostMapping("/reservations")
	public ReservationResponse reserve(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody ReserveRequest request) {
		return ReservationResponse.from(reservationService.reserve(request.resourceId(), actorId,
			request.startAt(), request.endAt(), request.purpose(), request.headcount(),
			request.note(), request.destination(), request.driver()));
	}

	/** 예약 취소(RSV-005): 본인은 자유, 타인 취소는 거점관리자 + 사유 필수(서비스 검증). */
	@PostMapping("/reservations/{id}/cancel")
	public ReservationResponse cancel(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@RequestBody CancelRequest request) {
		if (!actorId.equals(reservationService.reserverIdOf(id))) {
			requireLocationManager(actorId, resourceService.locationIdOfReservation(id));
		}
		return ReservationResponse.from(
			reservationService.cancel(id, actorId, request.reason()));
	}
}
