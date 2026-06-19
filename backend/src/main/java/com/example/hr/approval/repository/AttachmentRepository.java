package com.example.hr.approval.repository;

import com.example.hr.approval.entity.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

	List<Attachment> findByDocumentId(Long documentId);
}
