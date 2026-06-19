package com.example.hr.reservation.entity;

import com.example.hr.common.entity.BaseEntity;
import com.example.hr.reservation.domain.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 예약(RSV-002~005). 즉시 확정(ACTIVE). 기간은 반열림 [startAt, endAt).
 * 차량 예약은 행선지·운전자 보유(RSV-004). 취소 시 사유·취소자 기록(RSV-005).
 */
@Entity
@Table(name = "reservation")
public class Reservation extends BaseEntity {

	@Column(name = "resource_id", nullable = false)
	private Long resourceId;

	@Column(name = "reserver_id", nullable = false)
	private Long reserverId;

	@Column(name = "start_at", nullable = false)
	private OffsetDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private OffsetDateTime endAt;

	private String purpose;

	private int headcount;

	private String note;

	// 차량 전용(RSV-004)
	private String destination;

	private String driver;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status;

	@Column(name = "cancel_reason")
	private String cancelReason;

	@Column(name = "cancelled_by_id")
	private Long cancelledById;

	protected Reservation() {
	}

	public Reservation(Long resourceId, Long reserverId, OffsetDateTime startAt,
			OffsetDateTime endAt, String purpose, int headcount, String note) {
		this.resourceId = resourceId;
		this.reserverId = reserverId;
		this.startAt = startAt;
		this.endAt = endAt;
		this.purpose = purpose;
		this.headcount = headcount;
		this.note = note;
		this.status = ReservationStatus.ACTIVE; // 즉시 확정(RSV-002)
	}

	public void setVehicleInfo(String destination, String driver) {
		this.destination = destination;
		this.driver = driver;
	}

	/** 취소(RSV-005). 취소자·사유 기록. */
	public void cancel(Long cancelledById, String reason) {
		this.status = ReservationStatus.CANCELLED;
		this.cancelledById = cancelledById;
		this.cancelReason = reason;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public Long getReserverId() {
		return reserverId;
	}

	public OffsetDateTime getStartAt() {
		return startAt;
	}

	public OffsetDateTime getEndAt() {
		return endAt;
	}

	public String getPurpose() {
		return purpose;
	}

	public int getHeadcount() {
		return headcount;
	}

	public String getNote() {
		return note;
	}

	public String getDestination() {
		return destination;
	}

	public String getDriver() {
		return driver;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public Long getCancelledById() {
		return cancelledById;
	}
}
