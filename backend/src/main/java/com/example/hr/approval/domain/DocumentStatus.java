package com.example.hr.approval.domain;

/** 결재 문서 상태(AP-033): 임시저장→진행중→(보류)→승인완료|반려|회수. */
public enum DocumentStatus {
	DRAFT,
	IN_PROGRESS,
	ON_HOLD,
	APPROVED,
	REJECTED,
	WITHDRAWN
}
