package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** 위임(AP-022). 기간 없이 수동 해제까지 유효. 위임자 명의 + 수임자 대행 표시. */
@Entity
@Table(name = "mandate")
public class Mandate extends BaseEntity {

	@Column(name = "mandator_id", nullable = false)
	private Long mandatorId;

	@Column(name = "mandatee_id", nullable = false)
	private Long mandateeId;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "released_at")
	private OffsetDateTime releasedAt;

	protected Mandate() {
	}

	public Mandate(Long mandatorId, Long mandateeId) {
		this.mandatorId = mandatorId;
		this.mandateeId = mandateeId;
		this.active = true;
	}

	/** 수동 해제(AP-022 AC3). */
	public void release(OffsetDateTime at) {
		this.active = false;
		this.releasedAt = at;
	}

	public Long getMandatorId() {
		return mandatorId;
	}

	public Long getMandateeId() {
		return mandateeId;
	}

	public boolean isActive() {
		return active;
	}

	public OffsetDateTime getReleasedAt() {
		return releasedAt;
	}
}
