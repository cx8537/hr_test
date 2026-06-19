package com.example.hr.approval.service;

import com.example.hr.approval.domain.ExpenseCalculator;
import com.example.hr.approval.domain.ExpenseLine;
import com.example.hr.approval.domain.LeaveDaysCalculator;
import com.example.hr.approval.entity.AssetReqDoc;
import com.example.hr.approval.entity.AssetReqDocLine;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결재 양식 본문 저장(AP-041/042/044/045). 양식별 본문 테이블에 저장하며,
 * 지출/비품 금액은 {@link ExpenseCalculator}로(직접 저장 안 함), 휴가 일수는 {@link LeaveDaysCalculator}로
 * 공휴일(AP-043)을 즉시 반영해 산출한다. 일반 품의서는 본문이 비면 거부(AP-045 AC1).
 */
@Service
public class FormBodyService {

	/** 지출/비품 행 입력(품명, 수량, 단가, 비고). 금액은 저장하지 않고 계산한다. */
	public record LineSpec(String itemName, int quantity, BigDecimal unitPrice, String note) {
	}

	private final ExpenseDocRepository expenseDocRepository;
	private final ExpenseDocLineRepository expenseDocLineRepository;
	private final LeaveDocRepository leaveDocRepository;
	private final AssetReqDocRepository assetReqDocRepository;
	private final AssetReqDocLineRepository assetReqDocLineRepository;
	private final GeneralDocRepository generalDocRepository;
	private final HolidayRepository holidayRepository;

	public FormBodyService(ExpenseDocRepository expenseDocRepository,
			ExpenseDocLineRepository expenseDocLineRepository,
			LeaveDocRepository leaveDocRepository, AssetReqDocRepository assetReqDocRepository,
			AssetReqDocLineRepository assetReqDocLineRepository,
			GeneralDocRepository generalDocRepository, HolidayRepository holidayRepository) {
		this.expenseDocRepository = expenseDocRepository;
		this.expenseDocLineRepository = expenseDocLineRepository;
		this.leaveDocRepository = leaveDocRepository;
		this.assetReqDocRepository = assetReqDocRepository;
		this.assetReqDocLineRepository = assetReqDocLineRepository;
		this.generalDocRepository = generalDocRepository;
		this.holidayRepository = holidayRepository;
	}

	/** 지출결의서 저장(AP-041). 행 저장 후 합계(=Σ수량×단가)를 반환한다(금액은 저장하지 않음, AC1/AC2). */
	@Transactional
	public BigDecimal saveExpense(Long documentId, LocalDate expenseDate, String payee,
			List<LineSpec> lines) {
		requireLines(lines);
		expenseDocRepository.save(new ExpenseDoc(documentId, expenseDate, payee));
		List<ExpenseLine> calcLines = lines.stream()
			.map(l -> new ExpenseLine(l.quantity(), l.unitPrice()))
			.toList();
		BigDecimal total = ExpenseCalculator.total(calcLines); // 음수 수량/단가 검증 포함
		for (LineSpec l : lines) {
			expenseDocLineRepository.save(
				new ExpenseDocLine(documentId, l.itemName(), l.quantity(), l.unitPrice(), l.note()));
		}
		return total;
	}

	/** 휴가·근태 신청서 저장(AP-042). 등록 공휴일을 즉시 반영해 일수를 산출·저장한다(AC1/AC3). */
	@Transactional
	public LeaveDoc saveLeave(Long documentId, String leaveType, LocalDate startDate,
			LocalDate endDate, boolean halfDay, String reason, Long substituteId) {
		Set<LocalDate> holidays = holidayRepository.findByDateBetween(startDate, endDate).stream()
			.map(Holiday::getDate)
			.collect(Collectors.toSet());
		BigDecimal days = LeaveDaysCalculator.calculate(startDate, endDate, halfDay, holidays);
		return leaveDocRepository.save(new LeaveDoc(documentId, leaveType, startDate, endDate,
			halfDay, days, reason, substituteId));
	}

	/** 비품 신청서 저장(AP-044). 수령 거점 필수. 행 저장 후 예상 합계를 반환한다. */
	@Transactional
	public BigDecimal saveAssetReq(Long documentId, LocalDate desiredDate, Long receiveLocationId,
			String reason, List<LineSpec> lines) {
		if (receiveLocationId == null) {
			throw new IllegalArgumentException("수령 거점은 필수입니다."); // AP-044
		}
		requireLines(lines);
		assetReqDocRepository.save(new AssetReqDoc(documentId, desiredDate, receiveLocationId, reason));
		List<ExpenseLine> calcLines = lines.stream()
			.map(l -> new ExpenseLine(l.quantity(), l.unitPrice()))
			.toList();
		BigDecimal total = ExpenseCalculator.total(calcLines);
		for (LineSpec l : lines) {
			assetReqDocLineRepository.save(
				new AssetReqDocLine(documentId, l.itemName(), l.quantity(), l.unitPrice(), l.note()));
		}
		return total;
	}

	/** 일반 품의서 저장(AP-045). 본문이 비어 있으면 거부한다(AC1). */
	@Transactional
	public GeneralDoc saveGeneral(Long documentId, String body) {
		if (body == null || body.isBlank()) {
			throw new IllegalArgumentException("본문은 필수입니다."); // AP-045 AC1
		}
		return generalDocRepository.save(new GeneralDoc(documentId, body));
	}

	private void requireLines(List<LineSpec> lines) {
		if (lines == null || lines.isEmpty()) {
			throw new IllegalArgumentException("최소 한 개의 행이 필요합니다.");
		}
	}
}
