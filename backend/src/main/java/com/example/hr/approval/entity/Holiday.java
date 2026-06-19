package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** 공휴일(AP-043). 휴가 일수 계산(AP-042)에 사용. 날짜 유니크. */
@Entity
@Table(name = "holiday")
public class Holiday extends BaseEntity {

	@Column(name = "holiday_date", nullable = false, unique = true)
	private LocalDate date;

	@Column(nullable = false)
	private String name;

	protected Holiday() {
	}

	public Holiday(LocalDate date, String name) {
		this.date = date;
		this.name = name;
	}

	/** 공휴일 수정(AP-043). 날짜·명칭 변경 시 휴가 계산에 즉시 반영. */
	public void update(LocalDate date, String name) {
		this.date = date;
		this.name = name;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getName() {
		return name;
	}
}
