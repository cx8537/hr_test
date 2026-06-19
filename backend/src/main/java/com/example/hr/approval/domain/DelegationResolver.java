package com.example.hr.approval.domain;

import java.util.List;

/**
 * 대결·위임 런타임 판정(AP-021/022). 순수 함수 — 스냅샷의 원 결재자 대신
 * 실제 처리 시도자가 해당 단계를 처리할 수 있는지와 처리 유형을 판정한다.
 * 위임자 명의 우선 원칙(AP-022)에 따라 위임을 대결보다 먼저 본다. active 필터는 호출 측 책임(전달된 링크는 유효).
 */
public final class DelegationResolver {

	public enum ProcessingType { SELF, DEPUTY, MANDATEE }

	public record ProcessingRight(boolean allowed, ProcessingType type) {
		public static ProcessingRight denied() {
			return new ProcessingRight(false, null);
		}
	}

	/** 대결 사전 지정(원 결재자 → 대리인). */
	public record DelegationLink(Long approverId, Long deputyId) {
	}

	/** 위임(위임자 → 수임자). */
	public record MandateLink(Long mandatorId, Long mandateeId) {
	}

	private DelegationResolver() {
	}

	public static ProcessingRight resolve(Long originalApproverId, Long actorId,
			List<DelegationLink> delegations, List<MandateLink> mandates) {
		if (actorId.equals(originalApproverId)) {
			return new ProcessingRight(true, ProcessingType.SELF);
		}
		boolean mandated = mandates.stream().anyMatch(m ->
			m.mandatorId().equals(originalApproverId) && m.mandateeId().equals(actorId));
		if (mandated) {
			return new ProcessingRight(true, ProcessingType.MANDATEE); // 위임자 명의 우선(AP-022)
		}
		boolean delegated = delegations.stream().anyMatch(d ->
			d.approverId().equals(originalApproverId) && d.deputyId().equals(actorId));
		if (delegated) {
			return new ProcessingRight(true, ProcessingType.DEPUTY);
		}
		return ProcessingRight.denied();
	}
}
