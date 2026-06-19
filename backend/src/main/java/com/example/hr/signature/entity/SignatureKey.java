package com.example.hr.signature.entity;

import com.example.hr.common.entity.BaseEntity;
import com.example.hr.signature.domain.SignatureKeyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * 결재 공개키(FND-007/009). id가 곧 공개키 ID(서명마다 함께 기록 — FND-008 AC3).
 * 서버에는 공개키만 저장하며 개인키는 보관하지 않는다(FND-007 AC1).
 * 폐기 시 삭제하지 않고 status=REVOKED 로 보존한다(과거 서명 검증용 — FND-009).
 */
@Entity
@Table(name = "signature_key")
public class SignatureKey extends BaseEntity {

	@Column(name = "employee_id", nullable = false)
	private Long employeeId;

	@Column(name = "public_key", nullable = false, columnDefinition = "text")
	private String publicKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SignatureKeyStatus status;

	@Column(name = "revoked_at")
	private OffsetDateTime revokedAt;

	protected SignatureKey() {
	}

	public SignatureKey(Long employeeId, String publicKey) {
		this.employeeId = employeeId;
		this.publicKey = publicKey;
		this.status = SignatureKeyStatus.ACTIVE;
	}

	/** 폐기(보존, 삭제 금지). */
	public void revoke(OffsetDateTime at) {
		this.status = SignatureKeyStatus.REVOKED;
		this.revokedAt = at;
	}

	public boolean isActive() {
		return status == SignatureKeyStatus.ACTIVE;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public SignatureKeyStatus getStatus() {
		return status;
	}

	public OffsetDateTime getRevokedAt() {
		return revokedAt;
	}
}
