package com.example.hr.signature.domain;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 결재 서명 검증(FND-008). RSA-2048 / SHA256withRSA.
 * 브라우저 Web Crypto(RSASSA-PKCS1-v1_5 + SHA-256, SPKI export)와 동일 알고리즘으로 호환된다.
 * 순수 함수(프레임워크 비의존) — 공개키 조회는 호출 측이 담당한다.
 */
public final class SignatureVerifier {

	private SignatureVerifier() {
	}

	/** 데이터·서명값·공개키(PEM 또는 Base64 SPKI) → 유효성. 입력 오류 시 false. */
	public static boolean verify(byte[] data, String signatureBase64, String publicKeyText) {
		try {
			PublicKey publicKey = parsePublicKey(publicKeyText);
			byte[] signature = Base64.getDecoder().decode(signatureBase64);
			return verify(data, signature, publicKey);
		} catch (RuntimeException | GeneralSecurityException e) {
			return false;
		}
	}

	public static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
		try {
			Signature verifier = Signature.getInstance("SHA256withRSA");
			verifier.initVerify(publicKey);
			verifier.update(data);
			return verifier.verify(signature);
		} catch (GeneralSecurityException e) {
			return false;
		}
	}

	/** PEM 헤더/공백을 제거한 Base64 SPKI(X.509) 공개키를 복원한다. */
	public static PublicKey parsePublicKey(String publicKeyText) throws GeneralSecurityException {
		String base64 = publicKeyText
			.replaceAll("-----BEGIN [A-Z ]+-----", "")
			.replaceAll("-----END [A-Z ]+-----", "")
			.replaceAll("\\s", "");
		byte[] der = Base64.getDecoder().decode(base64);
		return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
	}
}
