package com.example.hr.document.dto;

import com.example.hr.document.domain.DocumentSource;
import com.example.hr.document.domain.DocumentVisibility;
import com.example.hr.document.entity.ArchiveDocument;

/** 문서 아카이브 DTO(DOC-004/006). */
public final class DocumentDtos {

	private DocumentDtos() {
	}

	public record DocumentResponse(Long id, String title, String fileName, DocumentSource source,
			DocumentVisibility visibility, Long folderId) {
		public static DocumentResponse from(ArchiveDocument d) {
			return new DocumentResponse(d.getId(), d.getTitle(), d.getFileName(), d.getSource(),
				d.getVisibility(), d.getFolderId());
		}
	}

	public record VersionResponse(Long id, Long archiveDocumentId, int versionNo) {
	}
}
