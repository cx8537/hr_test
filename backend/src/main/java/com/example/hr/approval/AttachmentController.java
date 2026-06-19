package com.example.hr.approval;

import com.example.hr.approval.dto.AttachmentDtos.Response;
import com.example.hr.approval.service.AttachmentService;
import com.example.hr.approval.service.AttachmentService.DownloadResult;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 결재 첨부 API(AP-040). 업로드/다운로드 모두 백엔드 경유 — presigned URL 미발급.
 * 다운로드는 서비스가 관여자 권한을 검사한 뒤 스트림을 중계한다.
 */
@RestController
@RequestMapping("/api/approval")
public class AttachmentController {

	private final AttachmentService attachmentService;

	public AttachmentController(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@PostMapping("/documents/{documentId}/attachments")
	public Response upload(@AuthenticationPrincipal Long actorId, @PathVariable Long documentId,
			@RequestParam("file") MultipartFile file) throws IOException {
		return Response.from(attachmentService.upload(documentId, actorId,
			file.getOriginalFilename(), file.getContentType(), file.getSize(),
			file.getInputStream()));
	}

	@GetMapping("/documents/{documentId}/attachments")
	public List<Response> list(@AuthenticationPrincipal Long actorId,
			@PathVariable Long documentId) {
		return attachmentService.list(documentId, actorId).stream().map(Response::from).toList();
	}

	@GetMapping("/attachments/{attachmentId}")
	public ResponseEntity<InputStreamResource> download(@AuthenticationPrincipal Long actorId,
			@PathVariable Long attachmentId) {
		DownloadResult result = attachmentService.download(attachmentId, actorId);
		String contentType = result.meta().getContentType() != null
			? result.meta().getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + result.meta().getFileName() + "\"")
			.contentType(MediaType.parseMediaType(contentType))
			.body(new InputStreamResource(result.stream()));
	}
}
