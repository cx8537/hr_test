package com.example.hr.approval.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/** AP-010~013/033: 결재 흐름 엔진(순차/병렬/합의 → 문서 상태). */
class ApprovalFlowEngineTest {

	private static Step seq(MemberState state) {
		return new Step(StepType.SEQUENTIAL, List.of(new StepMember(state)));
	}

	private static Step parallel(MemberState... states) {
		return new Step(StepType.PARALLEL, Arrays.stream(states).map(StepMember::new).toList());
	}

	private static Step consent(MemberState... states) {
		return new Step(StepType.CONSENT, Arrays.stream(states).map(StepMember::new).toList());
	}

	@Test
	void AP010_AC2_순차_전원승인_승인완료() {
		assertThat(ApprovalFlowEngine.evaluate(List.of(seq(MemberState.APPROVED), seq(MemberState.APPROVED))))
			.isEqualTo(DocumentStatus.APPROVED);
	}

	@Test
	void AP010_순차_일부미완료_진행중() {
		assertThat(ApprovalFlowEngine.evaluate(List.of(seq(MemberState.APPROVED), seq(MemberState.PENDING))))
			.isEqualTo(DocumentStatus.IN_PROGRESS);
	}

	@Test
	void AP010_AC1_1단계_미완료시_2단계_비활성() {
		List<Step> steps = List.of(seq(MemberState.PENDING), seq(MemberState.PENDING));
		assertThat(ApprovalFlowEngine.isStepActive(steps, 0)).isTrue();
		assertThat(ApprovalFlowEngine.isStepActive(steps, 1)).isFalse();
	}

	@Test
	void AP010_1단계완료시_2단계_활성() {
		List<Step> steps = List.of(seq(MemberState.APPROVED), seq(MemberState.PENDING));
		assertThat(ApprovalFlowEngine.isStepActive(steps, 1)).isTrue();
	}

	@Test
	void AP011_AC2_병렬_전원승인_다음단계_진행() {
		List<Step> steps = List.of(parallel(MemberState.APPROVED, MemberState.APPROVED), seq(MemberState.PENDING));
		assertThat(ApprovalFlowEngine.evaluate(steps)).isEqualTo(DocumentStatus.IN_PROGRESS);
		assertThat(ApprovalFlowEngine.isStepActive(steps, 1)).isTrue();
	}

	@Test
	void AP011_AC1_병렬_부분승인_미진행() {
		List<Step> steps = List.of(parallel(MemberState.APPROVED, MemberState.PENDING), seq(MemberState.PENDING));
		assertThat(ApprovalFlowEngine.evaluate(steps)).isEqualTo(DocumentStatus.IN_PROGRESS);
		assertThat(ApprovalFlowEngine.isStepActive(steps, 1)).isFalse(); // 1단계 미완료
	}

	@Test
	void AP012_AC1_병렬_1인반려_즉시전체반려() {
		assertThat(ApprovalFlowEngine.evaluate(List.of(parallel(MemberState.APPROVED, MemberState.REJECTED))))
			.isEqualTo(DocumentStatus.REJECTED);
	}

	@Test
	void AP013_AC1_합의거부_보류() {
		assertThat(ApprovalFlowEngine.evaluate(List.of(seq(MemberState.APPROVED), consent(MemberState.CONSENT_REJECTED))))
			.isEqualTo(DocumentStatus.ON_HOLD);
	}

	@Test
	void AP013_AC2_합의철회_재개() {
		// 철회 = CONSENT_REJECTED → PENDING 복원
		assertThat(ApprovalFlowEngine.evaluate(List.of(seq(MemberState.APPROVED), consent(MemberState.PENDING))))
			.isEqualTo(DocumentStatus.IN_PROGRESS);
	}

	@Test
	void AP013_AC4_합의_전원동의해야_진행() {
		assertThat(ApprovalFlowEngine.evaluate(List.of(consent(MemberState.APPROVED, MemberState.PENDING))))
			.isEqualTo(DocumentStatus.IN_PROGRESS);
		assertThat(ApprovalFlowEngine.evaluate(List.of(consent(MemberState.APPROVED, MemberState.APPROVED))))
			.isEqualTo(DocumentStatus.APPROVED);
	}

	@Test
	void 반려가_합의보류보다_우선() {
		List<Step> steps = List.of(parallel(MemberState.REJECTED), consent(MemberState.CONSENT_REJECTED));
		assertThat(ApprovalFlowEngine.evaluate(steps)).isEqualTo(DocumentStatus.REJECTED);
	}

	@Test
	void 전결_SKIPPED_단계는_완료로_간주() {
		assertThat(ApprovalFlowEngine.evaluate(List.of(seq(MemberState.APPROVED), seq(MemberState.SKIPPED))))
			.isEqualTo(DocumentStatus.APPROVED);
	}
}
