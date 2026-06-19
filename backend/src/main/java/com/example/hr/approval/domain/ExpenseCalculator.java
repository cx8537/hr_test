package com.example.hr.approval.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * 지출 금액·합계 계산(AP-041). 금액=수량×단가(직접 수정 불가, AC1), 합계=Σ행금액(AC2).
 * BigDecimal로 정밀 계산하며 음수 수량/단가는 거부한다. 순수 함수.
 */
public final class ExpenseCalculator {

	private ExpenseCalculator() {
	}

	public static BigDecimal amount(ExpenseLine line) {
		if (line.quantity() < 0) {
			throw new IllegalArgumentException("수량은 음수일 수 없습니다.");
		}
		if (line.unitPrice() == null || line.unitPrice().signum() < 0) {
			throw new IllegalArgumentException("단가는 음수일 수 없습니다.");
		}
		return line.unitPrice().multiply(BigDecimal.valueOf(line.quantity()));
	}

	public static BigDecimal total(List<ExpenseLine> lines) {
		return lines.stream()
			.map(ExpenseCalculator::amount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
