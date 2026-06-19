package com.example.hr.approval.repository;

import com.example.hr.approval.entity.Mandate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MandateRepository extends JpaRepository<Mandate, Long> {

	List<Mandate> findByActiveTrue();
}
