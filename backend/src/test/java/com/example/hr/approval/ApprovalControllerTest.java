package com.example.hr.approval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.approval.domain.StepType;
import com.example.hr.approval.dto.ApprovalDtos.ApproveRequest;
import com.example.hr.approval.dto.ApprovalDtos.ExpenseBody;
import com.example.hr.approval.dto.ApprovalDtos.LineItem;
import com.example.hr.approval.dto.ApprovalDtos.LineMember;
import com.example.hr.approval.dto.ApprovalDtos.RejectRequest;
import com.example.hr.approval.dto.ApprovalDtos.SubmitRequest;
import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.service.ApprovalService;
import com.example.hr.approval.service.FormBodyService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** AP-001/010/030/031/034: 결재 컨트롤러가 서비스에 위임하고 양식 본문을 분기 저장하는지 검증. */
class ApprovalControllerTest {

	private ApprovalService approvalService;
	private FormBodyService formBodyService;
	private ApprovalController controller;

	@BeforeEach
	void setUp() {
		approvalService = mock(ApprovalService.class);
		formBodyService = mock(FormBodyService.class);
		controller = new ApprovalController(approvalService, formBodyService);
	}

	@Test
	void AP001_지출_상신_본문저장() {
		ApprovalDocument doc = new ApprovalDocument(FormType.EXPENSE, "출장비", 100L, 9L);
		when(approvalService.submit(eq(100L), eq(9L), eq(FormType.EXPENSE), eq("출장비"), any()))
			.thenReturn(doc);
		SubmitRequest request = new SubmitRequest(FormType.EXPENSE, "출장비", 9L,
			List.of(new LineMember(1, 200L, StepType.SEQUENTIAL)),
			new ExpenseBody(null, "거래처A",
				List.of(new LineItem("KTX", 2, new BigDecimal("50000"), null))),
			null, null, null);

		var res = controller.submit(100L, request);

		assertThat(res.formType()).isEqualTo(FormType.EXPENSE);
		assertThat(res.title()).isEqualTo("출장비");
		verify(approvalService).submit(eq(100L), eq(9L), eq(FormType.EXPENSE), eq("출장비"), any());
		verify(formBodyService).saveExpense(any(), any(), eq("거래처A"), any());
		verify(formBodyService, never()).saveGeneral(any(), any());
	}

	@Test
	void AP045_일반_상신_본문저장() {
		ApprovalDocument doc = new ApprovalDocument(FormType.GENERAL, "품의", 100L, 9L);
		when(approvalService.submit(any(), any(), any(), any(), any())).thenReturn(doc);
		SubmitRequest request = new SubmitRequest(FormType.GENERAL, "품의", 9L,
			List.of(new LineMember(1, 200L, StepType.SEQUENTIAL)),
			null, null, null, "본문 내용");

		controller.submit(100L, request);

		verify(formBodyService).saveGeneral(any(), eq("본문 내용"));
		verify(formBodyService, never()).saveExpense(any(), any(), any(), any());
	}

	@Test
	void AP010_승인_서명전달() {
		when(approvalService.approve(eq(7L), eq(200L), eq(5L), eq("sig")))
			.thenReturn(DocumentStatus.APPROVED);

		var res = controller.approve(200L, 7L, new ApproveRequest(5L, "sig"));

		assertThat(res.status()).isEqualTo(DocumentStatus.APPROVED);
		verify(approvalService).approve(7L, 200L, 5L, "sig");
	}

	@Test
	void AP031_반려_사유전달() {
		when(approvalService.reject(eq(7L), eq(200L), eq("근거 부족")))
			.thenReturn(DocumentStatus.REJECTED);

		var res = controller.reject(200L, 7L, new RejectRequest("근거 부족"));

		assertThat(res.status()).isEqualTo(DocumentStatus.REJECTED);
		verify(approvalService).reject(7L, 200L, "근거 부족");
	}

	@Test
	void AP030_회수_위임() {
		when(approvalService.withdraw(eq(7L), eq(100L))).thenReturn(DocumentStatus.WITHDRAWN);

		var res = controller.withdraw(100L, 7L);

		assertThat(res.status()).isEqualTo(DocumentStatus.WITHDRAWN);
		verify(approvalService).withdraw(7L, 100L);
	}

	@Test
	void 회수는_승인서비스에만_위임() {
		when(approvalService.withdraw(anyLong(), anyLong())).thenReturn(DocumentStatus.WITHDRAWN);

		controller.withdraw(100L, 7L);

		verify(formBodyService, never()).saveGeneral(any(), any());
	}
}
