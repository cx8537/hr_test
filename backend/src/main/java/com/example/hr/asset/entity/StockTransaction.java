package com.example.hr.asset.entity;

import com.example.hr.asset.domain.StockTransactionType;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 입출고 이력(AST-003 AC1). 수량 비품의 현재 수량은 이 이력의 누적으로 산출(파생, AC2).
 * 결재 승인과 연동하지 않는다(AST-006, 수동 운영).
 */
@Entity
@Table(name = "stock_transaction")
public class StockTransaction extends BaseEntity {

	@Column(name = "asset_item_id", nullable = false)
	private Long assetItemId;

	@Enumerated(EnumType.STRING)
	@Column(name = "tx_type", nullable = false)
	private StockTransactionType type;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "occurred_at", nullable = false)
	private OffsetDateTime occurredAt;

	protected StockTransaction() {
	}

	public StockTransaction(Long assetItemId, StockTransactionType type, int quantity,
			OffsetDateTime occurredAt) {
		this.assetItemId = assetItemId;
		this.type = type;
		this.quantity = quantity;
		this.occurredAt = occurredAt;
	}

	public Long getAssetItemId() {
		return assetItemId;
	}

	public StockTransactionType getType() {
		return type;
	}

	public int getQuantity() {
		return quantity;
	}

	public OffsetDateTime getOccurredAt() {
		return occurredAt;
	}
}
