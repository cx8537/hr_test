package com.example.hr.signature.domain;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * FND-008: 브라우저 Web Crypto 호환성 교차 검증.
 * 아래 공개키·서명은 실제 Web Crypto(RSASSA-PKCS1-v1_5, SHA-256, 2048, SPKI export)로 생성한 고정 벡터다.
 * 백엔드 SignatureVerifier(SHA256withRSA)가 동일 알고리즘으로 검증함을 보장한다.
 */
class SignatureVerifierWebCryptoCompatTest {

	private static final String DATA = "HR_Test_05-cross-check";

	// Web Crypto exportKey("spki") → Base64
	private static final String WEB_CRYPTO_PUBLIC_KEY =
		"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAttTEyg6++G3wT/TGyxE43Hk+j88Aa5Ctm3BWvKRQj+L"
		+ "Raaq1blCIzQkHk0eJLx7fnHAzJ9sklA2r9uCLeLJduWCWl5oqEftVG1PCHlbAgdaPUK5tWp0gFCfb0KHutkwoim"
		+ "NZDJA29mSlUaCv8/VecVRCAccpGLnhgY08FoOx1UrMtJffBNfVtxm7BLFvc6oKbMfI28NIG6uG/MCCsk3dBm0hw"
		+ "kyagJJIdg9HgFLRHoWlAYEwcT9RdYWF8Uj1wCpeQtbJ8g449v9166wJ3hW9MBw70KX7eIknHBIG0A3tMhTuorXS"
		+ "rKyo6C7+8Q4Ys1wJknTDFmLzxjOZHRX/OsFdTQIDAQAB";

	// Web Crypto sign("RSASSA-PKCS1-v1_5", privateKey, data) → Base64
	private static final String WEB_CRYPTO_SIGNATURE =
		"iHgkGsb3cIaRMSX5x47MHw2YIZKi2rGFoAojn2mhLaTN7vgI6h5fZDhf8oI9VdZhPQXN4jQjr1l79/j4LIsahQf"
		+ "Z3DBn9AJ4aWqjZdv+Weut7AHsY+jHWOJpJpkvJtYxs62QkiPH5+H4hc5eY/ps+BO5LdgC56DOv0Min23dAsTRmh"
		+ "XnSDl6RXGkNgtWlfWIuwGOGBYM/0PJD96HZHLqNiQl41y0y9Yjg88iT82s7IbbRG14hNlAd2+Q3MutLdvMQZ1Gu"
		+ "IiJhr0DPmelN3vu+XTVIKEzfNaM7lA0RDSSF/H5XQMpIAZFyzpT5W376Rg2MsLq+cfTuDgRGpyx+e4a3Q==";

	@Test
	void FND008_WebCrypto_서명을_백엔드가_검증() {
		assertThat(SignatureVerifier.verify(DATA.getBytes(UTF_8), WEB_CRYPTO_SIGNATURE,
			WEB_CRYPTO_PUBLIC_KEY)).isTrue();
	}

	@Test
	void FND008_WebCrypto_다른데이터는_검증실패() {
		assertThat(SignatureVerifier.verify("tampered".getBytes(UTF_8), WEB_CRYPTO_SIGNATURE,
			WEB_CRYPTO_PUBLIC_KEY)).isFalse();
	}
}
