package com.example.hr.asset.entity;

import com.example.hr.asset.domain.IndividualAssetStatus;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * 개체 관리 비품(AST-002). 관리번호 유니크(AC1). 상태는 현재 값만 덮어쓰기(이력 없음, AC2).
 * 폐기해도 레코드는 보존하며 상태만 DISCARDED로 둔다(AST-004).
 */
@Entity
@Table(name = "individual_asset")
public class IndividualAsset extends BaseEntity {

	@Column(name = "asset_item_id", nullable = false)
	private Long assetItemId;

	@Column(name = "asset_number", nullable = false, unique = true)
	private String assetNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IndividualAssetStatus status;

	@Column(name = "acquisition_date")
	private LocalDate acquisitionDate;

	protected IndividualAsset() {
	}

	public IndividualAsset(Long assetItemId, String assetNumber, IndividualAssetStatus status,
			LocalDate acquisitionDate) {
		this.assetItemId = assetItemId;
		this.assetNumber = assetNumber;
		this.status = status;
		this.acquisitionDate = acquisitionDate;
	}

	/** 상태 변경(현재 값만 갱신, 과정 이력 저장 안 함 — AC2). */
	public void changeStatus(IndividualAssetStatus newStatus) {
		this.status = newStatus;
	}

	/** 폐기(레코드 보존, 상태만 DISCARDED — AST-004). */
	public void discard() {
		this.status = IndividualAssetStatus.DISCARDED;
	}

	public Long getAssetItemId() {
		return assetItemId;
	}

	public String getAssetNumber() {
		return assetNumber;
	}

	public IndividualAssetStatus getStatus() {
		return status;
	}

	public LocalDate getAcquisitionDate() {
		return acquisitionDate;
	}
}
