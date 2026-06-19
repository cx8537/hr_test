package com.example.hr.asset.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.hr.asset.domain.StockCalculator.StockEntry;
import java.util.List;
import org.junit.jupiter.api.Test;

/** AST-003: 수량 관리 비품 현재 수량 산출(입고합−출고합, 음수 거부). */
class StockCalculatorTest {

	private static StockEntry in(int q) {
		return new StockEntry(StockTransactionType.IN, q);
	}

	private static StockEntry out(int q) {
		return new StockEntry(StockTransactionType.OUT, q);
	}

	@Test
	void 빈_이력은_0() {
		assertThat(StockCalculator.currentQuantity(List.of())).isZero();
	}

	@Test
	void 입고_누적() {
		assertThat(StockCalculator.currentQuantity(List.of(in(10), in(5)))).isEqualTo(15);
	}

	@Test
	void AC2_입고합_빼기_출고합() {
		assertThat(StockCalculator.currentQuantity(List.of(in(10), out(3), in(2), out(4))))
			.isEqualTo(5); // 12 - 7
	}

	@Test
	void 경계값_정확히0() {
		assertThat(StockCalculator.currentQuantity(List.of(in(5), out(5)))).isZero();
	}

	@Test
	void AC3_출고로_음수가_되면_거부() {
		assertThatThrownBy(() -> StockCalculator.currentQuantity(List.of(in(3), out(4))))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void AC3_중간에_재고부족이면_거부() {
		assertThatThrownBy(() ->
			StockCalculator.currentQuantity(List.of(in(2), out(1), out(2), in(10))))
			.isInstanceOf(IllegalStateException.class); // 1 - 2 < 0 에서 즉시 거부
	}

	@Test
	void 음수_수량_입력_거부() {
		assertThatThrownBy(() -> StockCalculator.currentQuantity(List.of(in(-1))))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
