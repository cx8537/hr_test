package com.example.hr.document.repository;

import com.example.hr.document.entity.DocumentTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTagRepository extends JpaRepository<DocumentTag, Long> {

	List<DocumentTag> findByArchiveDocumentId(Long archiveDocumentId);

	List<DocumentTag> findByTagId(Long tagId);
}
