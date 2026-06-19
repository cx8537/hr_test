package com.example.hr.asset.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 수량 관리 비품(AST-003). 품목당 1건. 현재 수량은 저장하지 않고 {@link StockTransaction} 이력에서 파생한다
 * ({@code StockCalculator}). 입출고 누적 추적.
 */
@Entity
@Table(name = "quantity_asset")
public class QuantityAsset extends BaseEntity {

	@Column(name = "asset_item_id", nullable = false, unique = true)
	private Long assetItemId;

	protected QuantityAsset() {
	}

	public QuantityAsset(Long assetItemId) {
		this.assetItemId = assetItemId;
	}

	public Long getAssetItemId() {
		return assetItemId;
	}
}
