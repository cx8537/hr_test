package com.example.hr.signature.domain;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * FND-008: RSA-2048 서명 검증(SHA256withRSA, 브라우저 Web Crypto RSASSA-PKCS1-v1_5 호환).
 */
class SignatureVerifierTest {

	private static KeyPair keyPair;

	@BeforeAll
	static void generateKey() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		keyPair = generator.generateKeyPair();
	}

	private static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	private static String b64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	@Test
	void FND008_AC1_유효서명_검증통과() throws Exception {
		byte[] data = "결재문서-해시".getBytes(UTF_8);
		String sig = b64(sign(data, keyPair.getPrivate()));
		String publicKey = b64(keyPair.getPublic().getEncoded());

		assertThat(SignatureVerifier.verify(data, sig, publicKey)).isTrue();
	}

	@Test
	void FND008_AC2_변조된_데이터_검증실패() throws Exception {
		byte[] original = "원본 데이터".getBytes(UTF_8);
		String sig = b64(sign(original, keyPair.getPrivate()));
		String publicKey = b64(keyPair.getPublic().getEncoded());

		assertThat(SignatureVerifier.verify("변조된 데이터".getBytes(UTF_8), sig, publicKey)).isFalse();
	}

	@Test
	void FND008_AC2_다른키로_위조서명_검증실패() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair attacker = generator.generateKeyPair();

		byte[] data = "문서".getBytes(UTF_8);
		String forgedSig = b64(sign(data, attacker.getPrivate()));
		String publicKey = b64(keyPair.getPublic().getEncoded());

		assertThat(SignatureVerifier.verify(data, forgedSig, publicKey)).isFalse();
	}

	@Test
	void FND008_PEM형식_공개키_파싱() throws Exception {
		byte[] data = "문서".getBytes(UTF_8);
		String sig = b64(sign(data, keyPair.getPrivate()));
		String pem = "-----BEGIN PUBLIC KEY-----\n"
			+ b64(keyPair.getPublic().getEncoded()) + "\n-----END PUBLIC KEY-----";

		assertThat(SignatureVerifier.verify(data, sig, pem)).isTrue();
	}

	@Test
	void FND008_잘못된_입력_검증실패() {
		assertThat(SignatureVerifier.verify("x".getBytes(UTF_8), "not-base64!!", "also-bad"))
			.isFalse();
	}
}
