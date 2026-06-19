package com.example.hr.auth.repository;

import com.example.hr.auth.entity.RefreshToken;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	List<RefreshToken> findByEmployeeIdAndRevokedFalse(Long employeeId);
}
