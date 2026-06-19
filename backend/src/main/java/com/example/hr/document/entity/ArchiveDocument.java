package com.example.hr.document.entity;

import com.example.hr.common.entity.BaseEntity;
import com.example.hr.document.domain.DocumentSource;
import com.example.hr.document.domain.DocumentVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 아카이브 문서(DOC-001/002/005). 결재 자동 보관 또는 일반 업로드. 공개범위 속성 보유(DOC-005).
 * 결재 출처면 refDocumentId로 원본 결재 문서를 가리킨다(관여자 판정에 사용).
 */
@Entity
@Table(name = "archive_document")
public class ArchiveDocument extends BaseEntity {

	@Column(nullable = false)
	private String title;

	@Column(name = "file_name")
	private String fileName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentSource source;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentVisibility visibility;

	@Column(name = "folder_id")
	private Long folderId;

	@Column(name = "ref_document_id")
	private Long refDocumentId;

	@Column(name = "registrar_id")
	private Long registrarId;

	protected ArchiveDocument() {
	}

	public ArchiveDocument(String title, String fileName, DocumentSource source,
			DocumentVisibility visibility, Long folderId, Long refDocumentId, Long registrarId) {
		this.title = title;
		this.fileName = fileName;
		this.source = source;
		this.visibility = visibility;
		this.folderId = folderId;
		this.refDocumentId = refDocumentId;
		this.registrarId = registrarId;
	}

	public void moveToFolder(Long folderId) {
		this.folderId = folderId;
	}

	public String getTitle() {
		return title;
	}

	public String getFileName() {
		return fileName;
	}

	public DocumentSource getSource() {
		return source;
	}

	public DocumentVisibility getVisibility() {
		return visibility;
	}

	public Long getFolderId() {
		return folderId;
	}

	public Long getRefDocumentId() {
		return refDocumentId;
	}

	public Long getRegistrarId() {
		return registrarId;
	}
}
