package com.example.hr.org.entity;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 부서(FND-002). 2~3단계 고정 트리. 물리 삭제 금지(status로 비활성화).
 * 모듈 결합 최소화를 위해 상위 부서는 parentId(Long)로 보유한다.
 */
@Entity
@Table(name = "department")
public class Department extends BaseEntity {

	@Column(name = "dept_code", nullable = false, unique = true)
	private String deptCode;

	@Column(nullable = false)
	private String name;

	@Column(name = "parent_id")
	private Long parentId;

	@Column(nullable = false)
	private int level;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EntityStatus status;

	protected Department() {
	}

	public Department(String deptCode, String name, Long parentId, int level, EntityStatus status) {
		this.deptCode = deptCode;
		this.name = name;
		this.parentId = parentId;
		this.level = level;
		this.status = status;
	}

	public String getDeptCode() {
		return deptCode;
	}

	public String getName() {
		return name;
	}

	public Long getParentId() {
		return parentId;
	}

	public int getLevel() {
		return level;
	}

	public EntityStatus getStatus() {
		return status;
	}
}
