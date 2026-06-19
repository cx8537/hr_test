package com.example.hr.document;

import com.example.hr.document.dto.DocumentDtos.DocumentResponse;
import com.example.hr.document.dto.DocumentDtos.VersionResponse;
import com.example.hr.document.service.DocumentArchiveService;
import com.example.hr.document.service.DocumentArchiveService.DownloadResult;
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
 * 문서 아카이브 API(DOC-004/006/007). 검색·다운로드는 공개범위로 권한 검사(비관여자 403).
 * 모든 다운로드는 백엔드 경유(presigned 미사용, DOC-007).
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final DocumentArchiveService archiveService;

	public DocumentController(DocumentArchiveService archiveService) {
		this.archiveService = archiveService;
	}

	/** 검색(DOC-004): 제목·파일명. 볼 수 없는 문서는 결과에서 제외. */
	@GetMapping("/search")
	public List<DocumentResponse> search(@AuthenticationPrincipal Long actorId,
			@RequestParam String keyword) {
		return archiveService.search(keyword, actorId).stream().map(DocumentResponse::from).toList();
	}

	/** 새 버전 업로드(DOC-006). */
	@PostMapping("/{id}/versions")
	public VersionResponse addVersion(@AuthenticationPrincipal Long actorId, @PathVariable Long id,
			@RequestParam("file") MultipartFile file) throws IOException {
		var v = archiveService.addVersion(id, actorId, file.getContentType(), file.getSize(),
			file.getInputStream());
		return new VersionResponse(v.getId(), v.getArchiveDocumentId(), v.getVersionNo());
	}

	/** 다운로드(DOC-007): 권한 검사 후 최신 버전 스트림. 비관여자 403. */
	@GetMapping("/{id}/download")
	public ResponseEntity<InputStreamResource> download(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		DownloadResult result = archiveService.downloadLatest(id, actorId);
		String fileName = result.document().getFileName() != null
			? result.document().getFileName() : result.document().getTitle();
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.body(new InputStreamResource(result.stream()));
	}
}
