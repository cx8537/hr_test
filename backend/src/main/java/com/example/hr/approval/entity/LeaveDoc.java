package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** 휴가·근태 신청서 본문(AP-042). 총 일수(days)는 LeaveDaysCalculator 산출 결과를 저장. 문서와 1:1. */
@Entity
@Table(name = "leave_doc")
public class LeaveDoc extends BaseEntity {

	@Column(name = "document_id", nullable = false, unique = true)
	private Long documentId;

	@Column(name = "leave_type", nullable = false)
	private String leaveType;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "half_day", nullable = false)
	private boolean halfDay;

	@Column(nullable = false)
	private BigDecimal days;

	private String reason;

	@Column(name = "substitute_id")
	private Long substituteId;

	protected LeaveDoc() {
	}

	public LeaveDoc(Long documentId, String leaveType, LocalDate startDate, LocalDate endDate,
			boolean halfDay, BigDecimal days, String reason, Long substituteId) {
		this.documentId = documentId;
		this.leaveType = leaveType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.halfDay = halfDay;
		this.days = days;
		this.reason = reason;
		this.substituteId = substituteId;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public String getLeaveType() {
		return leaveType;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public boolean isHalfDay() {
		return halfDay;
	}

	public BigDecimal getDays() {
		return days;
	}

	public String getReason() {
		return reason;
	}

	public Long getSubstituteId() {
		return substituteId;
	}
}
