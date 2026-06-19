package com.example.hr.document.service;

import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.auth.AuthorizationService;
import com.example.hr.auth.domain.AccessRequest;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.common.storage.FileStorage;
import com.example.hr.document.domain.DocumentAccessPolicy;
import com.example.hr.document.domain.DocumentSource;
import com.example.hr.document.entity.ArchiveDocument;
import com.example.hr.document.entity.ArchiveVersion;
import com.example.hr.document.repository.ArchiveDocumentRepository;
import com.example.hr.document.repository.ArchiveVersionRepository;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문서 아카이브 조회·검색·버전·다운로드(DOC-004/005/006/007).
 * 다운로드·검색은 공개범위({@link DocumentAccessPolicy})로 관여자를 판정해 비관여자에게 거부한다(403).
 * 관여자 집합은 결재 출처 문서의 상신자 + 결재선(원 결재자·실제 처리자)에서 산출한다. 다운로드는 백엔드 경유.
 */
@Service
public class DocumentArchiveService {

	private static final AccessRequest SYS_ADMIN =
		new AccessRequest(Role.SYS_ADMIN, ScopeType.NONE, null);

	public record DownloadResult(ArchiveDocument document, ArchiveVersion version,
			InputStream stream) {
	}

	private final ArchiveDocumentRepository archiveRepository;
	private final ArchiveVersionRepository versionRepository;
	private final ApprovalDocumentRepository approvalDocumentRepository;
	private final ApprovalLineSnapshotRepository lineRepository;
	private final AuthorizationService authorizationService;
	private final FileStorage fileStorage;

	public DocumentArchiveService(ArchiveDocumentRepository archiveRepository,
			ArchiveVersionRepository versionRepository,
			ApprovalDocumentRepository approvalDocumentRepository,
			ApprovalLineSnapshotRepository lineRepository,
			AuthorizationService authorizationService, FileStorage fileStorage) {
		this.archiveRepository = archiveRepository;
		this.versionRepository = versionRepository;
		this.approvalDocumentRepository = approvalDocumentRepository;
		this.lineRepository = lineRepository;
		this.authorizationService = authorizationService;
		this.fileStorage = fileStorage;
	}

	/** 제목·파일명 검색(DOC-004). 요청자가 볼 수 없는 문서는 결과에서 제외(DOC-005). */
	@Transactional(readOnly = true)
	public List<ArchiveDocument> search(String keyword, Long requesterId) {
		boolean sysAdmin = authorizationService.isAllowed(requesterId, SYS_ADMIN);
		return archiveRepository
			.findByTitleContainingIgnoreCaseOrFileNameContainingIgnoreCase(keyword, keyword).stream()
			.filter(doc -> canView(doc, requesterId, sysAdmin))
			.toList();
	}

	/** 새 버전 추가(DOC-006): 다음 버전번호 채번 후 MinIO 저장. */
	@Transactional
	public ArchiveVersion addVersion(Long archiveDocumentId, Long uploaderId, String contentType,
			long size, InputStream data) {
		archiveRepository.findById(archiveDocumentId)
			.orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));
		int nextNo = versionRepository
			.findByArchiveDocumentIdOrderByVersionNoDesc(archiveDocumentId).stream()
			.findFirst().map(v -> v.getVersionNo() + 1).orElse(1);
		String objectKey = "archive/" + archiveDocumentId + "/v" + nextNo + "/" + UUID.randomUUID();
		fileStorage.put(objectKey, data, size, contentType);
		return versionRepository.save(
			new ArchiveVersion(archiveDocumentId, nextNo, objectKey, uploaderId));
	}

	/** 다운로드(DOC-007): 권한 검사 후 최신 버전 스트림 중계. 비관여자 거부(DOC-005 403). */
	@Transactional(readOnly = true)
	public DownloadResult downloadLatest(Long archiveDocumentId, Long requesterId) {
		ArchiveDocument doc = archiveRepository.findById(archiveDocumentId)
			.orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));
		boolean sysAdmin = authorizationService.isAllowed(requesterId, SYS_ADMIN);
		if (!canView(doc, requesterId, sysAdmin)) {
			throw new AccessDeniedException("문서 열람 권한이 없습니다."); // DOC-005/007
		}
		ArchiveVersion latest = versionRepository
			.findByArchiveDocumentIdOrderByVersionNoDesc(archiveDocumentId).stream()
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("버전이 없습니다."));
		return new DownloadResult(doc, latest, fileStorage.get(latest.getObjectKey()));
	}

	private boolean canView(ArchiveDocument doc, Long requesterId, boolean sysAdmin) {
		return DocumentAccessPolicy.canView(doc.getVisibility(), requesterId, involvedIds(doc),
			sysAdmin);
	}

	/** 관여자 집합: 결재 출처면 상신자 + 결재선(원 결재자·실제 처리자). 업로드 문서는 등록자. */
	private Set<Long> involvedIds(ArchiveDocument doc) {
		Set<Long> ids = new HashSet<>();
		if (doc.getSource() == DocumentSource.APPROVAL && doc.getRefDocumentId() != null) {
			approvalDocumentRepository.findById(doc.getRefDocumentId())
				.ifPresent(d -> ids.add(d.getDrafterId()));
			for (ApprovalLineSnapshot s : lineRepository.findByDocumentId(doc.getRefDocumentId())) {
				ids.add(s.getApproverId());
				if (s.getActedById() != null) {
					ids.add(s.getActedById());
				}
			}
		}
		if (doc.getRegistrarId() != null) {
			ids.add(doc.getRegistrarId());
		}
		return ids;
	}
}
