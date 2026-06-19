package com.example.hr.approval.repository;

import com.example.hr.approval.entity.Holiday;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

	/** 휴가 일수 계산용 기간 내 공휴일 조회(AP-042). */
	List<Holiday> findByDateBetween(LocalDate start, LocalDate end);

	boolean existsByDate(LocalDate date);
}
