package com.example.hr.document.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** DOC-005: 문서 공개범위 판정(전사공개·관여자한정·시스템관리자). */
class DocumentAccessPolicyTest {

	private static final Set<Long> INVOLVED = Set.of(100L, 200L, 300L); // 상신자·결재자 등

	@Test
	void AC3_전사공개는_누구나_허용() {
		assertThat(DocumentAccessPolicy.canView(
			DocumentVisibility.PUBLIC, 999L, Set.of(), false)).isTrue();
	}

	@Test
	void AC1_관여자한정_관여자는_허용() {
		assertThat(DocumentAccessPolicy.canView(
			DocumentVisibility.INVOLVED_ONLY, 200L, INVOLVED, false)).isTrue();
	}

	@Test
	void AC1_관여자한정_비관여자는_거부() {
		assertThat(DocumentAccessPolicy.canView(
			DocumentVisibility.INVOLVED_ONLY, 999L, INVOLVED, false)).isFalse();
	}

	@Test
	void 관여자한정_시스템관리자는_허용() {
		assertThat(DocumentAccessPolicy.canView(
			DocumentVisibility.INVOLVED_ONLY, 999L, INVOLVED, true)).isTrue();
	}

	@Test
	void 요청자가_null이면_거부() {
		assertThat(DocumentAccessPolicy.canView(
			DocumentVisibility.PUBLIC, null, INVOLVED, false)).isFalse();
	}

	@Test
	void 관여자집합이_null이면_관여자한정_비관리자_거부() {
		assertThat(DocumentAccessPolicy.canView(
			DocumentVisibility.INVOLVED_ONLY, 100L, null, false)).isFalse();
	}
}
