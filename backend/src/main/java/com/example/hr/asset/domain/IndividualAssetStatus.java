package com.example.hr.asset.domain;

/** 개체 비품 상태(AST-002). 현재 상태만 보관(이력 없음, 덮어쓰기). 폐기는 보존(AST-004). */
public enum IndividualAssetStatus {
	USING,
	STORAGE,
	REPAIR,
	DISCARDED
}
