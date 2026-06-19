package com.example.hr.approval.repository;

import com.example.hr.approval.entity.ApprovalLineSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalLineSnapshotRepository extends JpaRepository<ApprovalLineSnapshot, Long> {

	/** 현재 라운드 결재선을 단계 순서대로 조회. */
	List<ApprovalLineSnapshot> findByDocumentIdAndRoundOrderByStepNoAsc(Long documentId, int round);

	/** 문서의 모든 라운드 스냅샷(관여자 판정용). */
	List<ApprovalLineSnapshot> findByDocumentId(Long documentId);
}
