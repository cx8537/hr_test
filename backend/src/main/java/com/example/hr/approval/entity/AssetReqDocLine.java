package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** 비품 신청서 행(AP-044). 금액은 수량×예상단가 계산값으로 저장하지 않는다. */
@Entity
@Table(name = "asset_req_doc_line")
public class AssetReqDocLine extends BaseEntity {

	@Column(name = "document_id", nullable = false)
	private Long documentId;

	@Column(name = "item_name", nullable = false)
	private String itemName;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false)
	private BigDecimal unitPrice;

	private String note;

	protected AssetReqDocLine() {
	}

	public AssetReqDocLine(Long documentId, String itemName, int quantity, BigDecimal unitPrice,
			String note) {
		this.documentId = documentId;
		this.itemName = itemName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.note = note;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public String getItemName() {
		return itemName;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public String getNote() {
		return note;
	}
}
