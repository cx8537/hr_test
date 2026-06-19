package com.example.hr.location.entity;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * 거점(LOC-001). 물리 삭제 금지(status 비활성화). 좌표는 지도 핀으로 지정(LOC-002).
 * 담당자(managerId)는 1명만 지정하며 권한 부여와는 별개다(LOC-003).
 */
@Entity
@Table(name = "location")
public class Location extends BaseEntity {

	@Column(name = "location_code", nullable = false, unique = true)
	private String locationCode;

	@Column(nullable = false)
	private String name;

	private String address;

	private BigDecimal latitude;

	private BigDecimal longitude;

	private String contact;

	private String fax;

	@Column(name = "location_type")
	private String locationType;

	@Column(name = "manager_id")
	private Long managerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EntityStatus status;

	protected Location() {
	}

	public Location(String locationCode, String name, String address, String locationType,
			Long managerId) {
		this.locationCode = locationCode;
		this.name = name;
		this.address = address;
		this.locationType = locationType;
		this.managerId = managerId;
		this.status = EntityStatus.ACTIVE;
	}

	/** 기본 속성 수정(LOC-001). */
	public void update(String name, String address, String contact, String fax,
			String locationType) {
		this.name = name;
		this.address = address;
		this.contact = contact;
		this.fax = fax;
		this.locationType = locationType;
	}

	/** 좌표 지정(LOC-002: 지도 핀). */
	public void updateCoordinates(BigDecimal latitude, BigDecimal longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/** 담당자 1명 지정(LOC-001 AC2). 권한 부여와 별개(LOC-003). */
	public void assignManager(Long managerId) {
		this.managerId = managerId;
	}

	/** 비활성화(LOC-006: 물리 삭제 금지). 가능 여부는 서비스에서 자원 잔존을 먼저 판정한다. */
	public void deactivate() {
		this.status = EntityStatus.INACTIVE;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public String getContact() {
		return contact;
	}

	public String getFax() {
		return fax;
	}

	public String getLocationType() {
		return locationType;
	}

	public Long getManagerId() {
		return managerId;
	}

	public EntityStatus getStatus() {
		return status;
	}
}
