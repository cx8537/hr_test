package com.example.hr.document.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** 태그(DOC-003). 문서는 0개 이상의 태그를 가진다. */
@Entity
@Table(name = "tag")
public class Tag extends BaseEntity {

	@Column(nullable = false, unique = true)
	private String name;

	protected Tag() {
	}

	public Tag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
