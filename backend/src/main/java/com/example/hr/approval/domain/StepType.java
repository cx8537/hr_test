package com.example.hr.approval.domain;

/** 결재 단계 유형(AP-002/010~013). 한 단계는 단일 유형(혼재 불가). */
public enum StepType {
	SEQUENTIAL,
	PARALLEL,
	CONSENT
}
