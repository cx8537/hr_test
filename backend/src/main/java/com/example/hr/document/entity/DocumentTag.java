package com.example.hr.document.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** 문서-태그 매핑(DOC-003). 문서는 0개 이상의 태그를 가진다(N:M). */
@Entity
@Table(name = "document_tag")
public class DocumentTag extends BaseEntity {

	@Column(name = "archive_document_id", nullable = false)
	private Long archiveDocumentId;

	@Column(name = "tag_id", nullable = false)
	private Long tagId;

	protected DocumentTag() {
	}

	public DocumentTag(Long archiveDocumentId, Long tagId) {
		this.archiveDocumentId = archiveDocumentId;
		this.tagId = tagId;
	}

	public Long getArchiveDocumentId() {
		return archiveDocumentId;
	}

	public Long getTagId() {
		return tagId;
	}
}
