package com.example.hr.org.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** FND-002 AC1: 부서는 최대 3단계. 4단계 생성 거부. */
class DepartmentTreeTest {

	@Test
	void FND002_AC1_루트는_1단계() {
		assertThat(DepartmentTree.childLevel(null)).isEqualTo(1);
	}

	@Test
	void FND002_AC1_자식은_부모단계_플러스1() {
		assertThat(DepartmentTree.childLevel(1)).isEqualTo(2);
		assertThat(DepartmentTree.childLevel(2)).isEqualTo(3);
	}

	@Test
	void FND002_AC1_3단계까지_허용() {
		assertThat(DepartmentTree.canAttach(null)).isTrue(); // 루트(1단계)
		assertThat(DepartmentTree.canAttach(1)).isTrue(); // 2단계
		assertThat(DepartmentTree.canAttach(2)).isTrue(); // 3단계
	}

	@Test
	void FND002_AC1_4단계_거부() {
		assertThat(DepartmentTree.canAttach(3)).isFalse(); // 4단계 생성 시도
	}
}
