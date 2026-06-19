package com.example.hr.asset;

import com.example.hr.asset.dto.AssetDtos.CreateIndividualRequest;
import com.example.hr.asset.dto.AssetDtos.CreateItemRequest;
import com.example.hr.asset.dto.AssetDtos.IndividualResponse;
import com.example.hr.asset.dto.AssetDtos.ItemResponse;
import com.example.hr.asset.dto.AssetDtos.QuantityResponse;
import com.example.hr.asset.dto.AssetDtos.StatusRequest;
import com.example.hr.asset.dto.AssetDtos.StockRequest;
import com.example.hr.asset.service.AssetService;
import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 비품 관리 API(AST-001~004). 비품관리자(거점 범위) RBAC. 결재 연동 없음(AST-006).
 * 입출고/현재 수량은 수동 운영이며 결재 승인이 재고를 변경하지 않는다.
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

	private final AssetService assetService;
	private final AuthorizationService authorizationService;

	public AssetController(AssetService assetService, AuthorizationService authorizationService) {
		this.assetService = assetService;
		this.authorizationService = authorizationService;
	}

	/** 비품관리자(해당 거점 범위) 권한 요구. SYS_ADMIN은 전역 허용. */
	private void requireAssetManager(Long actorId, Long locationId) {
		authorizationService.checkAllowed(actorId,
			new AccessRequest(Role.ASSET_MANAGER, ScopeType.LOCATION, locationId));
	}

	@PostMapping("/items")
	public ItemResponse createItem(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody CreateItemRequest request) {
		requireAssetManager(actorId, request.locationId());
		return ItemResponse.from(assetService.registerItem(request.locationId(), request.name(),
			request.managementType()));
	}

	@PostMapping("/items/{itemId}/individuals")
	public IndividualResponse createIndividual(@AuthenticationPrincipal Long actorId,
			@PathVariable Long itemId, @Valid @RequestBody CreateIndividualRequest request) {
		requireAssetManager(actorId, assetService.locationIdOfItem(itemId));
		return IndividualResponse.from(assetService.registerIndividual(itemId,
			request.assetNumber(), request.status(), request.acquisitionDate()));
	}

	@PutMapping("/individuals/{individualId}/status")
	public IndividualResponse changeStatus(@AuthenticationPrincipal Long actorId,
			@PathVariable Long individualId, @Valid @RequestBody StatusRequest request) {
		requireAssetManager(actorId, assetService.locationIdOfIndividual(individualId));
		return IndividualResponse.from(
			assetService.changeIndividualStatus(individualId, request.status()));
	}

	@PostMapping("/individuals/{individualId}/discard")
	public IndividualResponse discard(@AuthenticationPrincipal Long actorId,
			@PathVariable Long individualId) {
		requireAssetManager(actorId, assetService.locationIdOfIndividual(individualId));
		return IndividualResponse.from(assetService.discardIndividual(individualId));
	}

	@PostMapping("/items/{itemId}/stock")
	public ResponseEntity<Void> recordStock(@AuthenticationPrincipal Long actorId,
			@PathVariable Long itemId, @Valid @RequestBody StockRequest request) {
		requireAssetManager(actorId, assetService.locationIdOfItem(itemId));
		assetService.recordStock(itemId, request.type(), request.quantity());
		return ResponseEntity.noContent().build();
	}

	@org.springframework.web.bind.annotation.GetMapping("/items/{itemId}/quantity")
	public QuantityResponse quantity(@AuthenticationPrincipal Long actorId,
			@PathVariable Long itemId) {
		requireAssetManager(actorId, assetService.locationIdOfItem(itemId));
		return new QuantityResponse(itemId, assetService.currentQuantity(itemId));
	}
}
