package com.example.hr.approval.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.hr.approval.domain.DelegationResolver.DelegationLink;
import com.example.hr.approval.domain.DelegationResolver.MandateLink;
import com.example.hr.approval.domain.DelegationResolver.ProcessingType;
import java.util.List;
import org.junit.jupiter.api.Test;

/** AP-021/022: 대결·위임 처리 권한 판정. */
class DelegationResolverTest {

	@Test
	void 본인_처리_SELF() {
		var right = DelegationResolver.resolve(100L, 100L, List.of(), List.of());
		assertThat(right.allowed()).isTrue();
		assertThat(right.type()).isEqualTo(ProcessingType.SELF);
	}

	@Test
	void AP021_대결_대리인_처리허용() {
		var right = DelegationResolver.resolve(100L, 300L,
			List.of(new DelegationLink(100L, 300L)), List.of());
		assertThat(right.allowed()).isTrue();
		assertThat(right.type()).isEqualTo(ProcessingType.DEPUTY);
	}

	@Test
	void AP022_위임_수임자_처리허용() {
		var right = DelegationResolver.resolve(100L, 400L,
			List.of(), List.of(new MandateLink(100L, 400L)));
		assertThat(right.allowed()).isTrue();
		assertThat(right.type()).isEqualTo(ProcessingType.MANDATEE);
	}

	@Test
	void 무관자_거부() {
		var right = DelegationResolver.resolve(100L, 999L,
			List.of(new DelegationLink(100L, 300L)), List.of(new MandateLink(100L, 400L)));
		assertThat(right.allowed()).isFalse();
	}

	@Test
	void AP022_위임자명의_우선_위임이_대결보다_우선() {
		// actor가 같은 원결재자에 대해 대결·위임 모두 가진 경우 위임(MANDATEE) 우선
		var right = DelegationResolver.resolve(100L, 500L,
			List.of(new DelegationLink(100L, 500L)), List.of(new MandateLink(100L, 500L)));
		assertThat(right.type()).isEqualTo(ProcessingType.MANDATEE);
	}
}
