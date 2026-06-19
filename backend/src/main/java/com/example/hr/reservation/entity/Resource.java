package com.example.hr.reservation.entity;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.common.entity.BaseEntity;
import com.example.hr.reservation.domain.ResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 예약 자원(RSV-001). 거점 종속. 유형(차량/회의실)별 속성은 nullable로 보유.
 * 물리 삭제 금지(status). 비활성화 시 미래 예약 자동 취소(RSV-007, LIFE-C3는 후속).
 */
@Entity
@Table(name = "resource")
public class Resource extends BaseEntity {

	@Column(name = "location_id", nullable = false)
	private Long locationId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResourceType type;

	@Column(nullable = false)
	private String name;

	// 차량 전용
	@Column(name = "vehicle_model")
	private String vehicleModel;

	@Column(name = "vehicle_number")
	private String vehicleNumber;

	// 회의실 전용(비치 장비)
	private String equipment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EntityStatus status;

	protected Resource() {
	}

	public Resource(Long locationId, ResourceType type, String name) {
		this.locationId = locationId;
		this.type = type;
		this.name = name;
		this.status = EntityStatus.ACTIVE;
	}

	public void setVehicleInfo(String vehicleModel, String vehicleNumber) {
		this.vehicleModel = vehicleModel;
		this.vehicleNumber = vehicleNumber;
	}

	public void setEquipment(String equipment) {
		this.equipment = equipment;
	}

	/** 비활성화(물리 삭제 금지). 미래 예약 정리는 서비스에서 처리(RSV-007). */
	public void deactivate() {
		this.status = EntityStatus.INACTIVE;
	}

	public Long getLocationId() {
		return locationId;
	}

	public ResourceType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getVehicleModel() {
		return vehicleModel;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public String getEquipment() {
		return equipment;
	}

	public EntityStatus getStatus() {
		return status;
	}
}
