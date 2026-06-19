package com.example.hr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 골격 단계 스모크 테스트(DB 불필요).
 * 테스트 인프라(JUnit5 + AssertJ)와 컴파일이 동작하는지 확인한다.
 * 스프링 컨텍스트 로드 검증은 DB 연결이 준비되는 Phase 0-3 이후 추가한다.
 */
class SmokeTest {

	@Test
	void testInfrastructureWorks() {
		assertThat(1 + 1).isEqualTo(2);
	}
}
