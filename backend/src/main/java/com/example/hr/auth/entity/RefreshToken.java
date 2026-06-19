package com.example.hr.auth.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 리프레시 토큰(FND-003/004). 원문 대신 해시를 저장한다.
 * 로그아웃·퇴사 시 revoked 처리(Access 무효화는 Employee.token_version 증가로 처리).
 */
@Entity
@Table(name = "refresh_token")
public class RefreshToken extends BaseEntity {

	@Column(name = "employee_id", nullable = false)
	private Long employeeId;

	@Column(name = "token_hash", nullable = false)
	private String tokenHash;

	@Column(name = "issued_at", nullable = false)
	private OffsetDateTime issuedAt;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	@Column(nullable = false)
	private boolean revoked;

	protected RefreshToken() {
	}

	public RefreshToken(Long employeeId, String tokenHash, OffsetDateTime issuedAt,
			OffsetDateTime expiresAt) {
		this.employeeId = employeeId;
		this.tokenHash = tokenHash;
		this.issuedAt = issuedAt;
		this.expiresAt = expiresAt;
		this.revoked = false;
	}

	public void revoke() {
		this.revoked = true;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public OffsetDateTime getIssuedAt() {
		return issuedAt;
	}

	public OffsetDateTime getExpiresAt() {
		return expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}
}
