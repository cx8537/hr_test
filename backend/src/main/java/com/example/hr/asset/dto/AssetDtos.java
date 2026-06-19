package com.example.hr.asset.dto;

import com.example.hr.asset.domain.AssetManagementType;
import com.example.hr.asset.domain.IndividualAssetStatus;
import com.example.hr.asset.domain.StockTransactionType;
import com.example.hr.asset.entity.AssetItem;
import com.example.hr.asset.entity.IndividualAsset;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/** 비품 관리 DTO(AST-001~004). */
public final class AssetDtos {

	private AssetDtos() {
	}

	public record CreateItemRequest(@NotNull Long locationId, @NotBlank String name,
			@NotNull AssetManagementType managementType) {
	}

	public record ItemResponse(Long id, Long locationId, String name,
			AssetManagementType managementType) {
		public static ItemResponse from(AssetItem i) {
			return new ItemResponse(i.getId(), i.getLocationId(), i.getName(),
				i.getManagementType());
		}
	}

	public record CreateIndividualRequest(@NotBlank String assetNumber,
			@NotNull IndividualAssetStatus status, LocalDate acquisitionDate) {
	}

	public record StatusRequest(@NotNull IndividualAssetStatus status) {
	}

	public record IndividualResponse(Long id, Long assetItemId, String assetNumber,
			IndividualAssetStatus status, LocalDate acquisitionDate) {
		public static IndividualResponse from(IndividualAsset a) {
			return new IndividualResponse(a.getId(), a.getAssetItemId(), a.getAssetNumber(),
				a.getStatus(), a.getAcquisitionDate());
		}
	}

	public record StockRequest(@NotNull StockTransactionType type,
			@PositiveOrZero int quantity) {
	}

	public record QuantityResponse(Long assetItemId, int currentQuantity) {
	}
}
