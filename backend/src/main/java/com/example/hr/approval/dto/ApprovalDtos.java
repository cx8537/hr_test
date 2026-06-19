package com.example.hr.approval.dto;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.approval.domain.StepType;
import com.example.hr.approval.entity.ApprovalDocument;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 결재 API DTO(AP-001/010/030/031/034). */
public final class ApprovalDtos {

	private ApprovalDtos() {
	}

	/** 결재선 한 멤버(상신 요청). */
	public record LineMember(int stepNo, @NotNull Long approverId, @NotNull StepType stepType) {
	}

	/** 지출/비품 행. 금액은 서버에서 수량×단가로 계산(저장 안 함). */
	public record LineItem(@NotBlank String itemName, int quantity,
			@NotNull BigDecimal unitPrice, String note) {
	}

	public record ExpenseBody(LocalDate expenseDate, String payee, List<LineItem> lines) {
	}

	public record LeaveBody(@NotBlank String leaveType, @NotNull LocalDate startDate,
			@NotNull LocalDate endDate, boolean halfDay, String reason, Long substituteId) {
	}

	public record AssetReqBody(LocalDate desiredDate, @NotNull Long receiveLocationId,
			String reason, List<LineItem> lines) {
	}

	/** 상신 요청(공통 헤더 + 결재선 + 양식별 본문). 본문은 formType에 해당하는 필드만 사용. */
	public record SubmitRequest(@NotNull FormType formType, @NotBlank String title,
			@NotNull Long draftDeptId, @NotEmpty List<LineMember> line,
			ExpenseBody expense, LeaveBody leave, AssetReqBody assetReq, String generalBody) {
	}

	public record ApproveRequest(@NotNull Long publicKeyId, @NotBlank String signatureBase64) {
	}

	public record RejectRequest(@NotBlank String reason) {
	}

	public record DocumentResponse(Long id, FormType formType, String title, DocumentStatus status,
			int currentRound) {
		public static DocumentResponse from(ApprovalDocument d) {
			return new DocumentResponse(d.getId(), d.getFormType(), d.getTitle(), d.getStatus(),
				d.getCurrentRound());
		}
	}

	public record StatusResponse(DocumentStatus status) {
	}
}
