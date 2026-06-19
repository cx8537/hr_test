package com.example.hr.approval.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** AP-041: 지출 금액·합계 계산. */
class ExpenseCalculatorTest {

	@Test
	void AP041_AC1_금액_수량곱하기단가() {
		BigDecimal amount = ExpenseCalculator.amount(new ExpenseLine(3, new BigDecimal("1500")));

		assertThat(amount).isEqualByComparingTo("4500");
	}

	@Test
	void AP041_AC2_합계_행금액의합() {
		List<ExpenseLine> lines = List.of(
			new ExpenseLine(2, new BigDecimal("1000")),
			new ExpenseLine(1, new BigDecimal("500")),
			new ExpenseLine(3, new BigDecimal("100")));

		assertThat(ExpenseCalculator.total(lines)).isEqualByComparingTo("2800");
	}

	@Test
	void AP041_빈행_합계_0() {
		assertThat(ExpenseCalculator.total(List.of())).isEqualByComparingTo("0");
	}

	@Test
	void AP041_음수_수량_거부() {
		assertThatThrownBy(() -> ExpenseCalculator.amount(new ExpenseLine(-1, new BigDecimal("100"))))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void AP041_음수_단가_거부() {
		assertThatThrownBy(() -> ExpenseCalculator.amount(new ExpenseLine(1, new BigDecimal("-100"))))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
