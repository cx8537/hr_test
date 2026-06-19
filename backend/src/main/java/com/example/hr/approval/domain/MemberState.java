package com.example.hr.approval.domain;

/** 결재선 단계별 결재자 상태(AP-002 AC2). CONSENT_REJECTED=합의 거부(보류), SKIPPED=전결 생략. */
public enum MemberState {
	PENDING,
	APPROVED,
	REJECTED,
	CONSENT_REJECTED,
	SKIPPED
}
