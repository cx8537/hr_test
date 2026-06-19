package com.example.hr.auth.domain;

import java.util.Set;

/**
 * 시스템관리자 최소 1명 보장(LIFE-A5). 순수 함수.
 * 마지막 시스템관리자의 비활성화 또는 SYS_ADMIN 역할 회수는 거부한다(self-lockout 방지, AC1).
 * 시스템관리자가 2명 이상이면 그중 한 명의 비활성화·회수는 허용(AC2).
 */
public final class SysAdminGuard {

	private SysAdminGuard() {
	}

	/**
	 * 대상자를 시스템관리자에서 제외(비활성화·역할 회수)해도 되는지.
	 * 대상이 시스템관리자가 아니면 항상 허용(무관). 대상이 시스템관리자면 제외 후 1명 이상 남아야 허용.
	 *
	 * @param currentSysAdminIds 현재 시스템관리자 보유자 ID 집합
	 * @param targetId           비활성화/회수 대상자 ID
	 */
	public static boolean canRemove(Set<Long> currentSysAdminIds, Long targetId) {
		if (currentSysAdminIds == null || !currentSysAdminIds.contains(targetId)) {
			return true; // 대상이 시스템관리자가 아니면 LIFE-A5 무관
		}
		return currentSysAdminIds.size() >= 2; // 마지막 1명이면 거부(AC1), 2명 이상이면 허용(AC2)
	}

	/** 위반 시 예외(거부 사유 표준화). */
	public static void requireCanRemove(Set<Long> currentSysAdminIds, Long targetId) {
		if (!canRemove(currentSysAdminIds, targetId)) {
			throw new IllegalStateException("마지막 시스템관리자는 비활성화/권한 회수할 수 없습니다."); // LIFE-A5 AC1
		}
	}
}
