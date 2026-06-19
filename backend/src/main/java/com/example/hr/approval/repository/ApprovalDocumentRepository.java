package com.example.hr.approval.repository;

import com.example.hr.approval.entity.ApprovalDocument;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long> {

	/** 상태 전이 시 비관적 락(SELECT FOR UPDATE)으로 문서 행을 잠근다(AP-033 동시성). */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select d from ApprovalDocument d where d.id = :id")
	Optional<ApprovalDocument> findWithLockById(@Param("id") Long id);
}
