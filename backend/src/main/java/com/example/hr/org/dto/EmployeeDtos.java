package com.example.hr.org.dto;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 임직원 관리 DTO(FND-001). */
public final class EmployeeDtos {

	private EmployeeDtos() {
	}

	public record CreateRequest(
			@NotBlank String empNo,
			@NotBlank String loginId,
			@NotBlank String password,
			@NotBlank String name,
			@NotNull Long deptId,
			String position,
			String email,
			String phone) {
	}

	public record ResetPasswordRequest(@NotBlank String tempPassword) {
	}

	public record Response(Long id, String empNo, String loginId, String name, Long deptId,
			String position, String email, String phone, EntityStatus status,
			boolean mustChangePassword) {
		public static Response from(Employee e) {
			return new Response(e.getId(), e.getEmpNo(), e.getLoginId(), e.getName(), e.getDeptId(),
				e.getPosition(), e.getEmail(), e.getPhone(), e.getStatus(), e.isMustChangePassword());
		}
	}
}
