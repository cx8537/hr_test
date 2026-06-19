package com.example.hr.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.approval.entity.ExpenseDoc;
import com.example.hr.approval.entity.ExpenseDocLine;
import com.example.hr.approval.entity.GeneralDoc;
import com.example.hr.approval.entity.Holiday;
import com.example.hr.approval.entity.LeaveDoc;
import com.example.hr.approval.repository.AssetReqDocLineRepository;
import com.example.hr.approval.repository.AssetReqDocRepository;
import com.example.hr.approval.repository.ExpenseDocLineRepository;
import com.example.hr.approval.repository.ExpenseDocRepository;
import com.example.hr.approval.repository.GeneralDocRepository;
import com.example.hr.approval.repository.HolidayRepository;
import com.example.hr.approval.repository.LeaveDocRepository;
import com.example.hr.approval.service.FormBodyService.LineSpec;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** AP-041/042/044/045: 결재 양식 본문 저장(금액 자동·본문 필수·공휴일 즉시 반영·수령 거점 필수). */
class FormBodyServiceTest {

	private ExpenseDocRepository expenseDocRepository;
	private ExpenseDocLineRepository expenseDocLineRepository;
	private LeaveDocRepository leaveDocRepository;
	private AssetReqDocRepository assetReqDocRepository;
	private AssetReqDocLineRepository assetReqDocLineRepository;
	private GeneralDocRepository generalDocRepository;
	private HolidayRepository holidayRepository;
	private FormBodyService formBodyService;

	@BeforeEach
	void setUp() {
		expenseDocRepository = mock(ExpenseDocRepository.class);
		expenseDocLineRepository = mock(ExpenseDocLineRepository.class);
		leaveDocRepository = mock(LeaveDocRepository.class);
		assetReqDocRepository = mock(AssetReqDocRepository.class);
		assetReqDocLineRepository = mock(AssetReqDocLineRepository.class);
		generalDocRepository = mock(GeneralDocRepository.class);
		holidayRepository = mock(HolidayRepository.class);
		formBodyService = new FormBodyService(expenseDocRepository, expenseDocLineRepository,
			leaveDocRepository, assetReqDocRepository, assetReqDocLineRepository,
			generalDocRepository, holidayRepository);
	}

	@Test
	void AP041_지출_금액자동_합계() {
		when(expenseDocLineRepository.save(any(ExpenseDocLine.class)))
			.thenAnswer(inv -> inv.getArgument(0));
		List<LineSpec> lines = List.of(
			new LineSpec("노트북", 2, new BigDecimal("1500000"), null),
			new LineSpec("마우스", 3, new BigDecimal("20000"), "무선"));

		BigDecimal total = formBodyService.saveExpense(10L, LocalDate.of(2026, 6, 30), "거래처A", lines);

		// 2×1,500,000 + 3×20,000 = 3,060,000 (AC1/AC2)
		assertThat(total).isEqualByComparingTo("3060000");
		verify(expenseDocRepository).save(any(ExpenseDoc.class));
		verify(expenseDocLineRepository, times(2)).save(any(ExpenseDocLine.class));
	}

	@Test
	void AP045_AC1_일반_본문_필수() {
		assertThatThrownBy(() -> formBodyService.saveGeneral(11L, "   "))
			.isInstanceOf(IllegalArgumentException.class);
		verify(generalDocRepository, never()).save(any(GeneralDoc.class));
	}

	@Test
	void AP045_일반_본문_저장() {
		when(generalDocRepository.save(any(GeneralDoc.class))).thenAnswer(inv -> inv.getArgument(0));

		GeneralDoc saved = formBodyService.saveGeneral(11L, "출장 품의합니다.");

		assertThat(saved.getBody()).isEqualTo("출장 품의합니다.");
	}

	@Test
	void AP042_AC3_휴가_공휴일_즉시반영() {
		// 2026-06-01(월)~06-05(금) 평일 5일 중 06-03을 공휴일로 등록 → 4일
		LocalDate start = LocalDate.of(2026, 6, 1);
		LocalDate end = LocalDate.of(2026, 6, 5);
		when(holidayRepository.findByDateBetween(start, end))
			.thenReturn(List.of(new Holiday(LocalDate.of(2026, 6, 3), "임시공휴일")));
		when(leaveDocRepository.save(any(LeaveDoc.class))).thenAnswer(inv -> inv.getArgument(0));

		LeaveDoc saved = formBodyService.saveLeave(12L, "ANNUAL", start, end, false, "연차", null);

		assertThat(saved.getDays()).isEqualByComparingTo("4");
	}

	@Test
	void AP044_수령거점_필수() {
		List<LineSpec> lines = List.of(new LineSpec("의자", 1, new BigDecimal("80000"), null));

		assertThatThrownBy(() ->
			formBodyService.saveAssetReq(13L, LocalDate.of(2026, 7, 1), null, "신규입사", lines))
			.isInstanceOf(IllegalArgumentException.class);
		verify(assetReqDocRepository, never()).save(any());
	}

	@Test
	void AP044_비품신청_저장_합계() {
		when(assetReqDocLineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		List<LineSpec> lines = List.of(
			new LineSpec("의자", 2, new BigDecimal("80000"), null));

		BigDecimal total = formBodyService.saveAssetReq(13L, LocalDate.of(2026, 7, 1), 5L,
			"신규입사", lines);

		assertThat(total).isEqualByComparingTo("160000");
		verify(assetReqDocRepository).save(any());
		verify(assetReqDocLineRepository, times(1)).save(any());
	}
}
