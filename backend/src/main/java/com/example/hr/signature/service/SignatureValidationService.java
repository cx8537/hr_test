package com.example.hr.signature.service;

import com.example.hr.signature.domain.SignatureVerifier;
import com.example.hr.signature.entity.SignatureKey;
import org.springframework.stereotype.Service;

/**
 * 서명 검증 위임(FND-008). 공개키 ID로 저장된 공개키를 찾아(폐기 키도 검증 가능 — FND-009)
 * RSA-2048 서명을 검증한다. 결재 등 도메인 서비스가 주입받아 사용한다.
 */
@Service
public class SignatureValidationService {

	private final SignatureKeyService signatureKeyService;

	public SignatureValidationService(SignatureKeyService signatureKeyService) {
		this.signatureKeyService = signatureKeyService;
	}

	public boolean verify(Long publicKeyId, byte[] data, String signatureBase64) {
		SignatureKey key = signatureKeyService.getById(publicKeyId);
		return SignatureVerifier.verify(data, signatureBase64, key.getPublicKey());
	}
}
