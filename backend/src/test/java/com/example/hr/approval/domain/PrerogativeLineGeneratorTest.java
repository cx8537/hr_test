package com.example.hr.approval.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/** AP-020: 전결 결재선 생성기. */
class PrerogativeLineGeneratorTest {

	private static Step seq() {
		return new Step(StepType.SEQUENTIAL, List.of(new StepMember(MemberState.PENDING)));
	}

	@Test
	void AP020_AC1_전결단계위_생략() {
		List<Step> base = List.of(seq(), seq(), seq(), seq()); // 4단계
		List<Step> result = PrerogativeLineGenerator.apply(base, 1); // 전결 종료=인덱스1(2단계)

		assertThat(result).hasSize(2); // 1·2단계만, 상위 생략
	}

	@Test
	void AP020_전결없음_전체유지() {
		List<Step> base = List.of(seq(), seq(), seq());

		assertThat(PrerogativeLineGenerator.apply(base, null)).hasSize(3);
	}

	@Test
	void AP020_전결단계_마지막이면_전체유지() {
		List<Step> base = List.of(seq(), seq(), seq());

		assertThat(PrerogativeLineGenerator.apply(base, 2)).hasSize(3);
	}

	@Test
	void AP020_인덱스_범위초과_거부() {
		List<Step> base = List.of(seq(), seq());

		assertThatThrownBy(() -> PrerogativeLineGenerator.apply(base, 5))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
