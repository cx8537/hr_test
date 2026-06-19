package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * 지출결의서 푸터(AP-041). 행은 {@link ExpenseDocLine}로 분리 저장하며 금액·합계는 계산값(저장 안 함, AC1/AC2).
 */
@Entity
@Table(name = "expense_doc")
public class ExpenseDoc extends BaseEntity {

	@Column(name = "document_id", nullable = false, unique = true)
	private Long documentId;

	@Column(name = "expense_date")
	private LocalDate expenseDate;

	private String payee;

	protected ExpenseDoc() {
	}

	public ExpenseDoc(Long documentId, LocalDate expenseDate, String payee) {
		this.documentId = documentId;
		this.expenseDate = expenseDate;
		this.payee = payee;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public LocalDate getExpenseDate() {
		return expenseDate;
	}

	public String getPayee() {
		return payee;
	}
}
