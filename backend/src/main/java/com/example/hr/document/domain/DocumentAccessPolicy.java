package com.example.hr.document.domain;

import java.util.Set;

/**
 * 문서 열람 권한 판정(DOC-005). 순수 함수.
 * 전사공개(PUBLIC)는 모든 활성 사용자 허용(AC3). 관여자한정(INVOLVED_ONLY)은 관여자 또는 시스템관리자만(AC1/AC2).
 * 관여자 = 상신자 + 결재자(+ 합의/대결/수임자). 시스템관리자는 항상 허용.
 */
public final class DocumentAccessPolicy {

	private DocumentAccessPolicy() {
	}

	public static boolean canView(DocumentVisibility visibility, Long requesterId,
			Set<Long> involvedIds, boolean sysAdmin) {
		if (requesterId == null) {
			return false;
		}
		if (sysAdmin) {
			return true; // 시스템관리자는 전 범위 허용
		}
		return switch (visibility) {
			case PUBLIC -> true; // 전사공개: 모든 활성 사용자(AC3)
			case INVOLVED_ONLY -> involvedIds != null && involvedIds.contains(requesterId); // AC1/AC2
		};
	}
}
