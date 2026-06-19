package com.example.hr.approval;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.dto.ApprovalDtos.ApproveRequest;
import com.example.hr.approval.dto.ApprovalDtos.AssetReqBody;
import com.example.hr.approval.dto.ApprovalDtos.DocumentResponse;
import com.example.hr.approval.dto.ApprovalDtos.ExpenseBody;
import com.example.hr.approval.dto.ApprovalDtos.LeaveBody;
import com.example.hr.approval.dto.ApprovalDtos.LineItem;
import com.example.hr.approval.dto.ApprovalDtos.RejectRequest;
import com.example.hr.approval.dto.ApprovalDtos.StatusResponse;
import com.example.hr.approval.dto.ApprovalDtos.SubmitRequest;
import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.service.ApprovalService;
import com.example.hr.approval.service.ApprovalService.LineMemberSpec;
import com.example.hr.approval.service.FormBodyService;
import com.example.hr.approval.service.FormBodyService.LineSpec;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 결재 API(AP-001/010/030/031/034). 상신 시 헤더+결재선 스냅샷 고정 후 양식 본문을 함께 저장한다.
 * 처리 권한은 서비스에서 결재 차례·대결/위임·서명으로 판정한다.
 */
@RestController
@RequestMapping("/api/approval/documents")
public class ApprovalController {

	private final ApprovalService approvalService;
	private final FormBodyService formBodyService;

	public ApprovalController(ApprovalService approvalService, FormBodyService formBodyService) {
		this.approvalService = approvalService;
		this.formBodyService = formBodyService;
	}

	/** 상신(AP-001/002): 결재선 스냅샷 고정 + 양식 본문 저장. */
	@PostMapping
	public DocumentResponse submit(@AuthenticationPrincipal Long actorId,
			@Valid @RequestBody SubmitRequest request) {
		List<LineMemberSpec> line = request.line().stream()
			.map(m -> new LineMemberSpec(m.stepNo(), m.approverId(), m.stepType()))
			.toList();
		ApprovalDocument document = approvalService.submit(actorId, request.draftDeptId(),
			request.formType(), request.title(), line);
		saveBody(document.getId(), request);
		return DocumentResponse.from(document);
	}

	/** 승인(AP-010/034): 본인 서명 전달. */
	@PostMapping("/{id}/approve")
	public StatusResponse approve(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@Valid @RequestBody ApproveRequest request) {
		DocumentStatus status = approvalService.approve(id, actorId, request.publicKeyId(),
			request.signatureBase64());
		return new StatusResponse(status);
	}

	/** 반려(AP-031): 사유 필수. */
	@PostMapping("/{id}/reject")
	public StatusResponse reject(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@Valid @RequestBody RejectRequest request) {
		DocumentStatus status = approvalService.reject(id, actorId, request.reason());
		return new StatusResponse(status);
	}

	/** 회수(AP-030): 상신자만, 무승인일 때만. */
	@PostMapping("/{id}/withdraw")
	public StatusResponse withdraw(@AuthenticationPrincipal Long actorId, @PathVariable Long id) {
		DocumentStatus status = approvalService.withdraw(id, actorId);
		return new StatusResponse(status);
	}

	/** formType별 양식 본문 저장 분기(AP-041/042/044/045). */
	private void saveBody(Long documentId, SubmitRequest request) {
		switch (request.formType()) {
			case EXPENSE -> {
				ExpenseBody body = request.expense();
				formBodyService.saveExpense(documentId, body.expenseDate(), body.payee(),
					toLineSpecs(body.lines()));
			}
			case LEAVE -> {
				LeaveBody body = request.leave();
				formBodyService.saveLeave(documentId, body.leaveType(), body.startDate(),
					body.endDate(), body.halfDay(), body.reason(), body.substituteId());
			}
			case ASSET_REQ -> {
				AssetReqBody body = request.assetReq();
				formBodyService.saveAssetReq(documentId, body.desiredDate(), body.receiveLocationId(),
					body.reason(), toLineSpecs(body.lines()));
			}
			case GENERAL -> formBodyService.saveGeneral(documentId, request.generalBody());
		}
	}

	private List<LineSpec> toLineSpecs(List<LineItem> items) {
		if (items == null) {
			throw new IllegalArgumentException("양식 행이 필요합니다.");
		}
		return items.stream()
			.map(i -> new LineSpec(i.itemName(), i.quantity(), i.unitPrice(), i.note()))
			.toList();
	}
}
