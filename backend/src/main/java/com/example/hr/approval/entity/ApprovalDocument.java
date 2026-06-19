package com.example.hr.approval.entity;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 결재 문서(AP-002/033). 상신 부서(draftDeptId)는 상신 시점 스냅샷.
 * 결재선은 별도 {@link ApprovalLineSnapshot}으로 복사·고정한다. currentRound는 재상신 라운드(AP-032).
 */
@Entity
@Table(name = "approval_document")
public class ApprovalDocument extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "form_type", nullable = false)
	private FormType formType;

	@Column(nullable = false)
	private String title;

	@Column(name = "drafter_id", nullable = false)
	private Long drafterId;

	@Column(name = "draft_dept_id", nullable = false)
	private Long draftDeptId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentStatus status;

	@Column(name = "current_round", nullable = false)
	private int currentRound;

	protected ApprovalDocument() {
	}

	public ApprovalDocument(FormType formType, String title, Long drafterId, Long draftDeptId) {
		this.formType = formType;
		this.title = title;
		this.drafterId = drafterId;
		this.draftDeptId = draftDeptId;
		this.status = DocumentStatus.DRAFT;
		this.currentRound = 1;
	}

	public void changeStatus(DocumentStatus newStatus) {
		this.status = newStatus;
	}

	/** 재상신: 다음 라운드로(이전 라운드 스냅샷은 보존 — AP-032). */
	public void nextRound() {
		this.currentRound++;
	}

	public FormType getFormType() {
		return formType;
	}

	public String getTitle() {
		return title;
	}

	public Long getDrafterId() {
		return drafterId;
	}

	public Long getDraftDeptId() {
		return draftDeptId;
	}

	public DocumentStatus getStatus() {
		return status;
	}

	public int getCurrentRound() {
		return currentRound;
	}
}
