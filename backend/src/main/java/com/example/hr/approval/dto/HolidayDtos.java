package com.example.hr.approval.dto;

import com.example.hr.approval.entity.Holiday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** 공휴일 관리 DTO(AP-043). */
public final class HolidayDtos {

	private HolidayDtos() {
	}

	public record SaveRequest(@NotNull LocalDate date, @NotBlank String name) {
	}

	public record Response(Long id, LocalDate date, String name) {
		public static Response from(Holiday h) {
			return new Response(h.getId(), h.getDate(), h.getName());
		}
	}
}
