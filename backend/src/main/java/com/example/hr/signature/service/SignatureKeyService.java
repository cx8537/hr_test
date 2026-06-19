package com.example.hr.signature.service;

import com.example.hr.signature.domain.SignatureKeyStatus;
import com.example.hr.signature.entity.SignatureKey;
import com.example.hr.signature.repository.SignatureKeyRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결재 공개키 관리(FND-007/009). 공개키만 저장(개인키 미보관),
 * 폐기는 보존(삭제 금지), 재발급은 기존 활성 키 폐기 후 새 키 발급.
 */
@Service
public class SignatureKeyService {

	private final SignatureKeyRepository signatureKeyRepository;
	private final Clock clock;

	public SignatureKeyService(SignatureKeyRepository signatureKeyRepository, Clock clock) {
		this.signatureKeyRepository = signatureKeyRepository;
		this.clock = clock;
	}

	/** 키 발급(FND-007): 사용자가 생성한 공개키만 저장한다. */
	@Transactional
	public SignatureKey issue(Long employeeId, String publicKey) {
		return signatureKeyRepository.save(new SignatureKey(employeeId, publicKey));
	}

	/** 폐기(FND-009): 삭제하지 않고 status=REVOKED 로 보존. */
	@Transactional
	public void revoke(Long keyId) {
		SignatureKey key = signatureKeyRepository.findById(keyId)
			.orElseThrow(() -> new IllegalArgumentException("공개키를 찾을 수 없습니다."));
		key.revoke(OffsetDateTime.now(clock));
	}

	/** 재발급(FND-009): 기존 활성 키를 모두 폐기하고 새 키를 발급. 과거 서명 검증을 위해 폐기 키는 보존. */
	@Transactional
	public SignatureKey reissue(Long employeeId, String newPublicKey) {
		OffsetDateTime now = OffsetDateTime.now(clock);
		signatureKeyRepository.findByEmployeeIdAndStatus(employeeId, SignatureKeyStatus.ACTIVE)
			.forEach(key -> key.revoke(now));
		return signatureKeyRepository.save(new SignatureKey(employeeId, newPublicKey));
	}

	@Transactional(readOnly = true)
	public List<SignatureKey> findActiveByEmployee(Long employeeId) {
		return signatureKeyRepository.findByEmployeeIdAndStatus(employeeId, SignatureKeyStatus.ACTIVE);
	}

	/** 서명 검증 시 사용 공개키 ID로 조회(폐기 키도 검증 가능 — FND-008/009 AC1). */
	@Transactional(readOnly = true)
	public SignatureKey getById(Long keyId) {
		return signatureKeyRepository.findById(keyId)
			.orElseThrow(() -> new IllegalArgumentException("공개키를 찾을 수 없습니다."));
	}
}
