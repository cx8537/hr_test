package com.example.hr.approval.domain;

import java.util.List;

/**
 * 결재 흐름 엔진(AP-010~013/033). 순수 함수 — 결재선 스냅샷의 멤버 상태로 문서 상태를 산출하고,
 * 단계 활성 여부(순차 순서 강제)를 판정한다. DB·프레임워크 비의존.
 *
 * 판정 규칙(우선순위):
 * 1) 어느 멤버라도 REJECTED → REJECTED (병렬 1인 반려 즉시 전체 반려, AP-012 / 일반 반려 AP-031)
 * 2) 어느 멤버라도 CONSENT_REJECTED → ON_HOLD (합의 거부 보류, AP-013; 철회 시 PENDING 복원→재개)
 * 3) 모든 단계 완료(전 멤버 APPROVED/SKIPPED) → APPROVED (AP-010 AC2, 전결 SKIPPED 포함)
 * 4) 그 외 → IN_PROGRESS
 */
public final class ApprovalFlowEngine {

	private ApprovalFlowEngine() {
	}

	public static DocumentStatus evaluate(List<Step> steps) {
		if (anyMemberInState(steps, MemberState.REJECTED)) {
			return DocumentStatus.REJECTED;
		}
		if (anyMemberInState(steps, MemberState.CONSENT_REJECTED)) {
			return DocumentStatus.ON_HOLD;
		}
		boolean allComplete = steps.stream().allMatch(ApprovalFlowEngine::isComplete);
		return allComplete ? DocumentStatus.APPROVED : DocumentStatus.IN_PROGRESS;
	}

	/** 해당 단계가 현재 처리 가능한지(이전 단계 모두 완료 + 본 단계 미완료). 순차 순서 강제(AP-010 AC1). */
	public static boolean isStepActive(List<Step> steps, int stepIndex) {
		if (stepIndex < 0 || stepIndex >= steps.size()) {
			return false;
		}
		for (int i = 0; i < stepIndex; i++) {
			if (!isComplete(steps.get(i))) {
				return false;
			}
		}
		return !isComplete(steps.get(stepIndex));
	}

	/** 단계 완료 = 전 멤버가 APPROVED 또는 SKIPPED(병렬·합의는 전원, AP-011/013 AC4). */
	private static boolean isComplete(Step step) {
		return step.members().stream()
			.allMatch(m -> m.state() == MemberState.APPROVED || m.state() == MemberState.SKIPPED);
	}

	private static boolean anyMemberInState(List<Step> steps, MemberState state) {
		return steps.stream().flatMap(s -> s.members().stream())
			.anyMatch(m -> m.state() == state);
	}
}
