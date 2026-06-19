package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** 대결 사전 지정(AP-021). 결재자가 부재 시 대신할 대리인을 지정한다. */
@Entity
@Table(name = "delegation")
public class Delegation extends BaseEntity {

	@Column(name = "approver_id", nullable = false)
	private Long approverId;

	@Column(name = "deputy_id", nullable = false)
	private Long deputyId;

	@Column(nullable = false)
	private boolean active;

	protected Delegation() {
	}

	public Delegation(Long approverId, Long deputyId) {
		this.approverId = approverId;
		this.deputyId = deputyId;
		this.active = true;
	}

	public void deactivate() {
		this.active = false;
	}

	public Long getApproverId() {
		return approverId;
	}

	public Long getDeputyId() {
		return deputyId;
	}

	public boolean isActive() {
		return active;
	}
}
