package com.example.hr.location.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 기본 자원 카운터(AST/RSV 미도입 상태). 항상 0건을 반환한다.
 * AST/RSV 모듈이 실제 집계 빈을 등록하면 그 빈이 우선한다(@ConditionalOnMissingBean).
 */
@Configuration
public class EmptyLocationResourceCounter {

	@Bean
	@ConditionalOnMissingBean(LocationResourceCounter.class)
	public LocationResourceCounter emptyLocationResourceCounter() {
		return locationId -> new LocationResourceCounter.Counts(0, 0);
	}
}
