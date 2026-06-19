package com.example.hr.approval.repository;

import com.example.hr.approval.entity.ApprovalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long> {
}
