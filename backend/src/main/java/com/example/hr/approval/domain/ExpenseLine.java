package com.example.hr.approval.domain;

import java.math.BigDecimal;

/** 지출결의서 행(AP-041). 금액은 수량×단가로 계산되며 직접 보관하지 않는다(수정 불가). */
public record ExpenseLine(int quantity, BigDecimal unitPrice) {
}
