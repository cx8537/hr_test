package com.example.hr.approval.service;

import com.example.hr.approval.entity.Holiday;
import com.example.hr.approval.repository.HolidayRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공휴일 관리(AP-043). 시스템관리자가 등록·수정·삭제하며, 변경은 휴가 일수 계산(AP-042)에 즉시 반영(AC1).
 * 공휴일은 참조 데이터라 물리 삭제를 허용한다(소프트삭제 대상 아님).
 */
@Service
public class HolidayService {

	private final HolidayRepository holidayRepository;

	public HolidayService(HolidayRepository holidayRepository) {
		this.holidayRepository = holidayRepository;
	}

	@Transactional
	public Holiday create(LocalDate date, String name) {
		if (date == null) {
			throw new IllegalArgumentException("공휴일 날짜는 필수입니다.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("공휴일 명칭은 필수입니다.");
		}
		if (holidayRepository.existsByDate(date)) {
			throw new IllegalStateException("이미 등록된 공휴일입니다: " + date);
		}
		return holidayRepository.save(new Holiday(date, name));
	}

	@Transactional
	public Holiday update(Long id, LocalDate date, String name) {
		if (date == null) {
			throw new IllegalArgumentException("공휴일 날짜는 필수입니다.");
		}
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("공휴일 명칭은 필수입니다.");
		}
		Holiday holiday = holidayRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("공휴일을 찾을 수 없습니다."));
		holiday.update(date, name);
		return holiday;
	}

	@Transactional
	public void delete(Long id) {
		if (!holidayRepository.existsById(id)) {
			throw new IllegalArgumentException("공휴일을 찾을 수 없습니다.");
		}
		holidayRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public List<Holiday> findByYear(int year) {
		return holidayRepository.findByDateBetween(
			LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
	}
}
