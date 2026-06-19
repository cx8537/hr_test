package com.example.hr.org.domain;

/**
 * 조직 트리 규칙(FND-002): 부서는 2~3단계 고정 트리, 최대 3단계.
 * 루트는 1단계, 자식은 부모 단계 +1. 4단계 생성은 거부(AC1).
 */
public final class DepartmentTree {

	public static final int MAX_LEVEL = 3;

	private DepartmentTree() {
	}

	/** 부모 단계로부터 자식 단계 산출(루트는 parentLevel=null → 1단계). */
	public static int childLevel(Integer parentLevel) {
		return parentLevel == null ? 1 : parentLevel + 1;
	}

	/** 해당 부모 아래에 부서를 추가할 수 있는지(최대 3단계 이내). */
	public static boolean canAttach(Integer parentLevel) {
		return childLevel(parentLevel) <= MAX_LEVEL;
	}
}
