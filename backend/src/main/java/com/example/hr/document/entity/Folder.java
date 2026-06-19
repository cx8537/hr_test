package com.example.hr.document.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** 아카이브 폴더(DOC-003). 문서는 폴더 1개에 속한다. 상위 폴더는 parentId로 보유. */
@Entity
@Table(name = "folder")
public class Folder extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(name = "parent_id")
	private Long parentId;

	protected Folder() {
	}

	public Folder(String name, Long parentId) {
		this.name = name;
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public Long getParentId() {
		return parentId;
	}
}
