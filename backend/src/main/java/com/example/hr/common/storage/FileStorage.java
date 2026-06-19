package com.example.hr.common.storage;

import java.io.InputStream;

/**
 * 파일 저장 추상화(AP-040). 모든 접근은 백엔드 경유이며 presigned URL을 외부에 노출하지 않는다.
 * 구현은 MinIO({@link MinioFileStorage})이며, 단위 테스트는 이 인터페이스를 모킹한다.
 */
public interface FileStorage {

	/** 객체를 저장한다(키는 호출자가 생성). */
	void put(String objectKey, InputStream data, long size, String contentType);

	/** 객체를 스트림으로 읽는다(백엔드가 권한 검사 후 전달). */
	InputStream get(String objectKey);

	/** 객체를 삭제한다. */
	void delete(String objectKey);
}
