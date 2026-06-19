package com.example.hr.approval.repository;

import com.example.hr.approval.entity.ExpenseDoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseDocRepository extends JpaRepository<ExpenseDoc, Long> {
}
