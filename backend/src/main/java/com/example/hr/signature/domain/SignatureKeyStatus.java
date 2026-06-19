package com.example.hr.signature.domain;

/** 결재 공개키 상태(FND-007/009). 폐기 키도 삭제하지 않고 보존한다. */
public enum SignatureKeyStatus {
	ACTIVE,
	REVOKED
}
