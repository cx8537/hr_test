// @vitest-environment node
import { describe, expect, it } from "vitest";
import {
  exportPrivateKeyBase64,
  exportPublicKeyBase64,
  generateSigningKeyPair,
  signData,
  verifyData,
} from "@/lib/crypto";

// FND-007/008: Web Crypto 키 생성·서명·검증(RSASSA-PKCS1-v1_5 / SHA-256).
describe("crypto (FND-007/008)", () => {
  it("키 생성 → 서명 → 자체 검증 통과", async () => {
    const keyPair = await generateSigningKeyPair();
    const sig = await signData(keyPair.privateKey, "결재문서-해시");
    expect(await verifyData(keyPair.publicKey, "결재문서-해시", sig)).toBe(true);
  });

  it("변조된 데이터는 검증 실패", async () => {
    const keyPair = await generateSigningKeyPair();
    const sig = await signData(keyPair.privateKey, "원본");
    expect(await verifyData(keyPair.publicKey, "변조", sig)).toBe(false);
  });

  it("공개키 SPKI / 개인키 PKCS8 Base64 export", async () => {
    const keyPair = await generateSigningKeyPair();
    const pub = await exportPublicKeyBase64(keyPair.publicKey);
    const priv = await exportPrivateKeyBase64(keyPair.privateKey);
    expect(pub.length).toBeGreaterThan(0);
    expect(priv.length).toBeGreaterThan(0);
  });
});
