package com.example.hr.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.approval.entity.Holiday;
import com.example.hr.approval.repository.HolidayRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** AP-043: 공휴일 관리(등록·수정·삭제, 중복 거부). */
class HolidayServiceTest {

	private HolidayRepository holidayRepository;
	private HolidayService holidayService;

	@BeforeEach
	void setUp() {
		holidayRepository = mock(HolidayRepository.class);
		holidayService = new HolidayService(holidayRepository);
	}

	@Test
	void AP043_공휴일_등록() {
		LocalDate date = LocalDate.of(2026, 1, 1);
		when(holidayRepository.existsByDate(date)).thenReturn(false);
		when(holidayRepository.save(any(Holiday.class))).thenAnswer(inv -> inv.getArgument(0));

		Holiday saved = holidayService.create(date, "신정");

		assertThat(saved.getDate()).isEqualTo(date);
		assertThat(saved.getName()).isEqualTo("신정");
		verify(holidayRepository).save(any(Holiday.class));
	}

	@Test
	void AP043_중복날짜_등록_거부() {
		LocalDate date = LocalDate.of(2026, 1, 1);
		when(holidayRepository.existsByDate(date)).thenReturn(true);

		assertThatThrownBy(() -> holidayService.create(date, "신정"))
			.isInstanceOf(IllegalStateException.class);
		verify(holidayRepository, never()).save(any(Holiday.class));
	}

	@Test
	void AP043_공휴일_수정() {
		Holiday holiday = new Holiday(LocalDate.of(2026, 1, 1), "신정");
		when(holidayRepository.findById(1L)).thenReturn(Optional.of(holiday));

		holidayService.update(1L, LocalDate.of(2026, 3, 1), "삼일절");

		assertThat(holiday.getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
		assertThat(holiday.getName()).isEqualTo("삼일절");
	}

	@Test
	void AP043_없는공휴일_삭제_거부() {
		when(holidayRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> holidayService.delete(99L))
			.isInstanceOf(IllegalArgumentException.class);
		verify(holidayRepository, never()).deleteById(any());
	}
}
