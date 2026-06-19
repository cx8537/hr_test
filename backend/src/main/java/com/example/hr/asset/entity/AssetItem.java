package com.example.hr.asset.entity;

import com.example.hr.asset.domain.AssetManagementType;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 비품 품목(AST-001). 거점에 종속. 관리 유형(개체/수량)은 등록 시 명시하며 변경 불가(AC1/AC2).
 * 개체는 {@link IndividualAsset}, 수량은 {@link QuantityAsset}로 분리 저장한다.
 */
@Entity
@Table(name = "asset_item")
public class AssetItem extends BaseEntity {

	@Column(name = "location_id", nullable = false)
	private Long locationId;

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "management_type", nullable = false)
	private AssetManagementType managementType;

	protected AssetItem() {
	}

	public AssetItem(Long locationId, String name, AssetManagementType managementType) {
		this.locationId = locationId;
		this.name = name;
		this.managementType = managementType;
	}

	public Long getLocationId() {
		return locationId;
	}

	public String getName() {
		return name;
	}

	public AssetManagementType getManagementType() {
		return managementType;
	}
}
