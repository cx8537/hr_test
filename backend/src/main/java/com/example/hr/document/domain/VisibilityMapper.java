package com.example.hr.document.domain;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;

/**
 * 양식·상태 → 문서 공개범위 매핑(DOC-005 1차 매핑). 순수 함수.
 * 반려·회수 문서(양식 불문)와 휴가·근태 양식은 관여자한정, 그 외 결재 완료·일반 업로드는 전사공개.
 * 매핑을 코드 한 곳에 모아 향후 설정화(AC4)를 쉽게 한다.
 */
public final class VisibilityMapper {

	private VisibilityMapper() {
	}

	public static DocumentVisibility forApproval(FormType formType, DocumentStatus status) {
		if (status == DocumentStatus.REJECTED || status == DocumentStatus.WITHDRAWN) {
			return DocumentVisibility.INVOLVED_ONLY; // 반려·회수(AC2)
		}
		if (formType == FormType.LEAVE) {
			return DocumentVisibility.INVOLVED_ONLY; // 휴가·근태 민감 양식(AC1)
		}
		return DocumentVisibility.PUBLIC; // 지출·비품·일반 승인완료(AC3)
	}

	public static DocumentVisibility forUpload() {
		return DocumentVisibility.PUBLIC; // 일반 업로드 문서(규정·양식·매뉴얼)
	}
}
