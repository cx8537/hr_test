package com.example.hr.approval.repository;

import com.example.hr.approval.entity.ExpenseDocLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseDocLineRepository extends JpaRepository<ExpenseDocLine, Long> {

	List<ExpenseDocLine> findByDocumentId(Long documentId);
}
