package com.example.hr.approval.dto;

import com.example.hr.approval.entity.Attachment;

/** 첨부 DTO(AP-040). 객체키는 외부에 노출하지 않는다. */
public final class AttachmentDtos {

	private AttachmentDtos() {
	}

	public record Response(Long id, Long documentId, String fileName, String contentType,
			long sizeBytes, Long uploaderId) {
		public static Response from(Attachment a) {
			return new Response(a.getId(), a.getDocumentId(), a.getFileName(), a.getContentType(),
				a.getSizeBytes(), a.getUploaderId());
		}
	}
}
