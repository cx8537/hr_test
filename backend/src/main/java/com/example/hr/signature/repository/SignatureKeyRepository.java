package com.example.hr.signature.repository;

import com.example.hr.signature.domain.SignatureKeyStatus;
import com.example.hr.signature.entity.SignatureKey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureKeyRepository extends JpaRepository<SignatureKey, Long> {

	List<SignatureKey> findByEmployeeIdAndStatus(Long employeeId, SignatureKeyStatus status);

	List<SignatureKey> findByEmployeeId(Long employeeId);
}
