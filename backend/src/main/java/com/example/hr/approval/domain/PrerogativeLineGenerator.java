package com.example.hr.approval.domain;

import java.util.List;

/**
 * 전결 결재선 생성기(AP-020). 기본 결재선에서 전결 종료 단계까지만 남기고 상위 단계를 생략한다.
 * 전결 단계가 지정되지 않으면(null) 전체 결재선을 그대로 유지한다. 순수 함수.
 */
public final class PrerogativeLineGenerator {

	private PrerogativeLineGenerator() {
	}

	/**
	 * @param baseLine             기본 결재선(단계 목록)
	 * @param prerogativeStepIndex 전결 종료 단계(0-based). null이면 전결 없음.
	 * @return 전결 단계까지만 포함한 결재선(AC1). 전결 없으면 전체.
	 */
	public static List<Step> apply(List<Step> baseLine, Integer prerogativeStepIndex) {
		if (prerogativeStepIndex == null) {
			return List.copyOf(baseLine);
		}
		if (prerogativeStepIndex < 0 || prerogativeStepIndex >= baseLine.size()) {
			throw new IllegalArgumentException("전결 종료 단계 인덱스가 결재선 범위를 벗어났습니다.");
		}
		return List.copyOf(baseLine.subList(0, prerogativeStepIndex + 1));
	}
}
