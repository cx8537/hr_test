package com.example.hr.org.dto;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Department;
import jakarta.validation.constraints.NotBlank;

/** 부서 관리 DTO(FND-002). */
public final class DepartmentDtos {

	private DepartmentDtos() {
	}

	public record CreateRequest(@NotBlank String deptCode, @NotBlank String name, Long parentId) {
	}

	public record Response(Long id, String deptCode, String name, Long parentId, int level,
			EntityStatus status) {
		public static Response from(Department d) {
			return new Response(d.getId(), d.getDeptCode(), d.getName(), d.getParentId(),
				d.getLevel(), d.getStatus());
		}
	}
}
