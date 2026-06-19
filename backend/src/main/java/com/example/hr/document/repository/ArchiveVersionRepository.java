package com.example.hr.document.repository;

import com.example.hr.document.entity.ArchiveVersion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveVersionRepository extends JpaRepository<ArchiveVersion, Long> {

	/** 최신 버전 우선 조회(DOC-006 AC2). */
	List<ArchiveVersion> findByArchiveDocumentIdOrderByVersionNoDesc(Long archiveDocumentId);
}
