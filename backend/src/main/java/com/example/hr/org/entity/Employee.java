package com.example.hr.org.entity;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 임직원(FND-001). 사번·로그인ID 고유, 1인 1부서(deptId 필수), 물리 삭제 금지.
 * token_version은 로그아웃·퇴사·강제만료 시 증가시켜 기존 Access 토큰 무효화(FND-004).
 */
@Entity
@Table(name = "employee")
public class Employee extends BaseEntity {

	@Column(name = "emp_no", nullable = false, unique = true)
	private String empNo;

	@Column(name = "login_id", nullable = false, unique = true)
	private String loginId;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String name;

	@Column(name = "dept_id", nullable = false)
	private Long deptId;

	@Column
	private String position;

	@Column
	private String email;

	@Column
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EntityStatus status;

	@Column(name = "token_version", nullable = false)
	private int tokenVersion;

	@Column(name = "must_change_password", nullable = false)
	private boolean mustChangePassword;

	protected Employee() {
	}

	public Employee(String empNo, String loginId, String passwordHash, String name, Long deptId,
			String position, String email, String phone, EntityStatus status,
			int tokenVersion, boolean mustChangePassword) {
		this.empNo = empNo;
		this.loginId = loginId;
		this.passwordHash = passwordHash;
		this.name = name;
		this.deptId = deptId;
		this.position = position;
		this.email = email;
		this.phone = phone;
		this.status = status;
		this.tokenVersion = tokenVersion;
		this.mustChangePassword = mustChangePassword;
	}

	/** 로그아웃·퇴사·강제만료 시 토큰 버전을 올려 기존 Access 토큰을 무효화한다(FND-004). */
	public void incrementTokenVersion() {
		this.tokenVersion++;
	}

	/** 비활성화(소프트 삭제, FND-001 AC3/AC4). 기존 토큰도 즉시 무효화. */
	public void deactivate() {
		this.status = EntityStatus.INACTIVE;
		incrementTokenVersion();
	}

	/** 관리자 비밀번호 리셋(FND 주석): 임시 비밀번호 발급 + 최초 로그인 변경 강제 + 기존 세션 무효화. */
	public void resetPassword(String newPasswordHash) {
		this.passwordHash = newPasswordHash;
		this.mustChangePassword = true;
		incrementTokenVersion();
	}

	public String getEmpNo() {
		return empNo;
	}

	public String getLoginId() {
		return loginId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getName() {
		return name;
	}

	public Long getDeptId() {
		return deptId;
	}

	public String getPosition() {
		return position;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public EntityStatus getStatus() {
		return status;
	}

	public int getTokenVersion() {
		return tokenVersion;
	}

	public boolean isMustChangePassword() {
		return mustChangePassword;
	}
}
