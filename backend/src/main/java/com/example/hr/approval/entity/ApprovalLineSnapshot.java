package com.example.hr.approval.entity;

import com.example.hr.approval.domain.MemberState;
import com.example.hr.approval.domain.StepType;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 결재선 스냅샷(AP-002). 상신 시 결재선 구조를 복사·고정한다(AC1/AC2).
 * approverId는 원 결재자 식별자로 보존되며(AC3), 대결/위임의 실제 처리자는 서명(AP-021/022)에서 별도 기록.
 * round로 재상신 라운드를 구분해 과거 라운드를 보존한다(AP-032).
 */
@Entity
@Table(name = "approval_line_snapshot")
public class ApprovalLineSnapshot extends BaseEntity {

	@Column(name = "document_id", nullable = false)
	private Long documentId;

	@Column(nullable = false)
	private int round;

	@Column(name = "step_no", nullable = false)
	private int stepNo;

	@Column(name = "approver_id", nullable = false)
	private Long approverId;

	@Enumerated(EnumType.STRING)
	@Column(name = "step_type", nullable = false)
	private StepType stepType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MemberState state;

	@Column(name = "acted_at")
	private OffsetDateTime actedAt;

	protected ApprovalLineSnapshot() {
	}

	public ApprovalLineSnapshot(Long documentId, int round, int stepNo, Long approverId,
			StepType stepType) {
		this.documentId = documentId;
		this.round = round;
		this.stepNo = stepNo;
		this.approverId = approverId;
		this.stepType = stepType;
		this.state = MemberState.PENDING;
	}

	/** 결재자 처리(승인/반려/합의거부/생략 등)로 상태·시각 갱신. */
	public void act(MemberState newState, OffsetDateTime at) {
		this.state = newState;
		this.actedAt = at;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public int getRound() {
		return round;
	}

	public int getStepNo() {
		return stepNo;
	}

	public Long getApproverId() {
		return approverId;
	}

	public StepType getStepType() {
		return stepType;
	}

	public MemberState getState() {
		return state;
	}

	public OffsetDateTime getActedAt() {
		return actedAt;
	}
}
