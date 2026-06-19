package com.example.hr.document.repository;

import com.example.hr.document.entity.ArchiveDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveDocumentRepository extends JpaRepository<ArchiveDocument, Long> {

	/** 제목·파일명 검색(DOC-004, 본문 전문 검색 없음). */
	List<ArchiveDocument> findByTitleContainingIgnoreCaseOrFileNameContainingIgnoreCase(
			String title, String fileName);

	List<ArchiveDocument> findByFolderId(Long folderId);
}
