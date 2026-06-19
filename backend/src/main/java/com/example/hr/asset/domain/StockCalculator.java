package com.example.hr.asset.domain;

import java.util.List;

/**
 * 수량 관리 비품 현재 수량 산출(AST-003). 순수 함수.
 * 현재 수량 = 입고 합 − 출고 합(AC2). 입출고 이력을 시간 순서대로 누적 적용하며,
 * 어떤 출고로도 수량이 음수가 되면 거부한다(AC3, 재고 부족). 음수 수량 입력도 거부.
 */
public final class StockCalculator {

	/** 입출고 한 건(유형, 수량). 수량은 양수. */
	public record StockEntry(StockTransactionType type, int quantity) {
	}

	private StockCalculator() {
	}

	/** 이력(시간 순)을 누적 적용한 현재 수량. 출고로 음수가 되면 IllegalStateException. */
	public static int currentQuantity(List<StockEntry> history) {
		int quantity = 0;
		for (StockEntry entry : history) {
			if (entry.quantity() < 0) {
				throw new IllegalArgumentException("입출고 수량은 음수일 수 없습니다.");
			}
			quantity = switch (entry.type()) {
				case IN -> quantity + entry.quantity();
				case OUT -> {
					int next = quantity - entry.quantity();
					if (next < 0) {
						throw new IllegalStateException("재고가 부족하여 출고할 수 없습니다."); // AC3
					}
					yield next;
				}
			};
		}
		return quantity;
	}
}
