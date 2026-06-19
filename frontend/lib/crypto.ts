// 결재 서명용 키 생성·서명(FND-007/008). 브라우저 Web Crypto API 사용.
// 개인키는 네트워크로 전송하지 않으며, 발급 시 1회 다운로드해 사용자가 보관한다.
// 알고리즘은 백엔드 SignatureVerifier(SHA256withRSA)와 호환되는 RSASSA-PKCS1-v1_5 / SHA-256.
const ALGORITHM = { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" } as const;

function toBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = "";
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary);
}

function fromBase64(base64: string): Uint8Array {
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes;
}

export async function generateSigningKeyPair(): Promise<CryptoKeyPair> {
  return crypto.subtle.generateKey(
    { ...ALGORITHM, modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]) },
    true,
    ["sign", "verify"],
  );
}

/** 공개키 SPKI(X.509) Base64 — 서버 저장용(FND-007). */
export async function exportPublicKeyBase64(key: CryptoKey): Promise<string> {
  return toBase64(await crypto.subtle.exportKey("spki", key));
}

/** 개인키 PKCS8 Base64 — 1회 다운로드용(서버에 저장하지 않음). */
export async function exportPrivateKeyBase64(key: CryptoKey): Promise<string> {
  return toBase64(await crypto.subtle.exportKey("pkcs8", key));
}

/** 데이터 서명 → Base64(FND-008). */
export async function signData(privateKey: CryptoKey, data: string): Promise<string> {
  const signature = await crypto.subtle.sign(
    ALGORITHM,
    privateKey,
    new TextEncoder().encode(data),
  );
  return toBase64(signature);
}

/** 자체 검증(클라이언트 호환성 확인용). 서버 검증이 권위 있다. */
export async function verifyData(
  publicKey: CryptoKey,
  data: string,
  signatureBase64: string,
): Promise<boolean> {
  return crypto.subtle.verify(
    ALGORITHM,
    publicKey,
    fromBase64(signatureBase64) as BufferSource,
    new TextEncoder().encode(data),
  );
}
