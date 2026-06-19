package com.example.hr.document.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import org.junit.jupiter.api.Test;

/** DOC-005: 양식·상태 → 공개범위 매핑. */
class VisibilityMapperTest {

	@Test
	void 지출_승인완료는_전사공개() {
		assertThat(VisibilityMapper.forApproval(FormType.EXPENSE, DocumentStatus.APPROVED))
			.isEqualTo(DocumentVisibility.PUBLIC);
	}

	@Test
	void AC1_휴가는_관여자한정() {
		assertThat(VisibilityMapper.forApproval(FormType.LEAVE, DocumentStatus.APPROVED))
			.isEqualTo(DocumentVisibility.INVOLVED_ONLY);
	}

	@Test
	void AC2_반려문서는_양식불문_관여자한정() {
		assertThat(VisibilityMapper.forApproval(FormType.EXPENSE, DocumentStatus.REJECTED))
			.isEqualTo(DocumentVisibility.INVOLVED_ONLY);
	}

	@Test
	void AC2_회수문서는_관여자한정() {
		assertThat(VisibilityMapper.forApproval(FormType.GENERAL, DocumentStatus.WITHDRAWN))
			.isEqualTo(DocumentVisibility.INVOLVED_ONLY);
	}

	@Test
	void 일반업로드는_전사공개() {
		assertThat(VisibilityMapper.forUpload()).isEqualTo(DocumentVisibility.PUBLIC);
	}
}
