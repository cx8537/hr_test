package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** 일반 품의서 본문(AP-045). 본문(body)은 필수(AC1). 문서와 1:1. */
@Entity
@Table(name = "general_doc")
public class GeneralDoc extends BaseEntity {

	@Column(name = "document_id", nullable = false, unique = true)
	private Long documentId;

	@Column(nullable = false)
	private String body;

	protected GeneralDoc() {
	}

	public GeneralDoc(Long documentId, String body) {
		this.documentId = documentId;
		this.body = body;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public String getBody() {
		return body;
	}
}
