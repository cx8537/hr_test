package com.example.hr.approval.repository;

import com.example.hr.approval.entity.Delegation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelegationRepository extends JpaRepository<Delegation, Long> {

	List<Delegation> findByActiveTrue();
}
