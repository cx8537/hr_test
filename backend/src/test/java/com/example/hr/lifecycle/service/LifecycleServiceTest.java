package com.example.hr.lifecycle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.domain.FormType;
import com.example.hr.approval.domain.StepType;
import com.example.hr.approval.entity.ApprovalDocument;
import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.ScopeType;
import com.example.hr.auth.entity.EmployeeRoleScope;
import com.example.hr.auth.repository.EmployeeRoleScopeRepository;
import com.example.hr.common.domain.EntityStatus;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.EmployeeRepository;
import com.example.hr.signature.entity.SignatureKey;
import com.example.hr.signature.service.SignatureKeyService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** LIFE-A1~A3/A5: 퇴사 처리(비활성+키폐기+영향 결재 산출, 마지막 관리자 거부). */
class LifecycleServiceTest {

	private EmployeeRepository employeeRepository;
	private EmployeeRoleScopeRepository roleScopeRepository;
	private SignatureKeyService signatureKeyService;
	private ApprovalLineSnapshotRepository lineRepository;
	private ApprovalDocumentRepository documentRepository;
	private LifecycleService lifecycleService;

	@BeforeEach
	void setUp() {
		employeeRepository = mock(EmployeeRepository.class);
		roleScopeRepository = mock(EmployeeRoleScopeRepository.class);
		signatureKeyService = mock(SignatureKeyService.class);
		lineRepository = mock(ApprovalLineSnapshotRepository.class);
		documentRepository = mock(ApprovalDocumentRepository.class);
		lifecycleService = new LifecycleService(employeeRepository, roleScopeRepository,
			signatureKeyService, lineRepository, documentRepository);
	}

	private Employee employee() {
		return new Employee("E001", "user1", "hash", "홍길동", 9L, "사원", null, null,
			EntityStatus.ACTIVE, 0, false);
	}

	private EmployeeRoleScope sysAdmin(Long employeeId) {
		return new EmployeeRoleScope(employeeId, Role.SYS_ADMIN, ScopeType.NONE, null);
	}

	@Test
	void LIFE_A1_A3_퇴사시_비활성_및_키폐기() {
		Employee emp = employee();
		when(roleScopeRepository.findByRole(Role.SYS_ADMIN)).thenReturn(List.of());
		when(employeeRepository.findById(5L)).thenReturn(Optional.of(emp));
		when(signatureKeyService.findActiveByEmployee(5L)).thenReturn(List.of(
			new SignatureKey(5L, "pk1"), new SignatureKey(5L, "pk2")));
		when(lineRepository.findByApproverId(5L)).thenReturn(List.of());

		var result = lifecycleService.resign(5L);

		assertThat(emp.getStatus()).isEqualTo(EntityStatus.INACTIVE); // A1
		assertThat(result.revokedKeyCount()).isEqualTo(2);
		verify(signatureKeyService, org.mockito.Mockito.times(2)).revoke(any()); // A3
	}

	@Test
	void LIFE_A2_진행중_결재_영향문서_산출() {
		when(roleScopeRepository.findByRole(Role.SYS_ADMIN)).thenReturn(List.of());
		when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee()));
		when(signatureKeyService.findActiveByEmployee(5L)).thenReturn(List.of());
		when(lineRepository.findByApproverId(5L)).thenReturn(List.of(
			new ApprovalLineSnapshot(100L, 1, 1, 5L, StepType.SEQUENTIAL),
			new ApprovalLineSnapshot(200L, 1, 1, 5L, StepType.SEQUENTIAL)));
		ApprovalDocument inProgress = new ApprovalDocument(FormType.GENERAL, "진행중", 1L, 9L);
		inProgress.changeStatus(DocumentStatus.IN_PROGRESS);
		ApprovalDocument approved = new ApprovalDocument(FormType.GENERAL, "완료", 1L, 9L);
		approved.changeStatus(DocumentStatus.APPROVED);
		when(documentRepository.findById(100L)).thenReturn(Optional.of(inProgress));
		when(documentRepository.findById(200L)).thenReturn(Optional.of(approved));

		var result = lifecycleService.resign(5L);

		assertThat(result.affectedInProgressDocumentIds()).containsExactly(100L); // 진행중만
	}

	@Test
	void LIFE_A5_마지막_시스템관리자_퇴사_거부() {
		when(roleScopeRepository.findByRole(Role.SYS_ADMIN)).thenReturn(List.of(sysAdmin(5L)));

		assertThatThrownBy(() -> lifecycleService.resign(5L))
			.isInstanceOf(IllegalStateException.class);
		verify(employeeRepository, never()).findById(anyLong()); // 비활성 시도 전에 차단
	}

	@Test
	void LIFE_A5_시스템관리자_2명이면_1명_퇴사_허용() {
		when(roleScopeRepository.findByRole(Role.SYS_ADMIN))
			.thenReturn(List.of(sysAdmin(5L), sysAdmin(6L)));
		when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee()));
		when(signatureKeyService.findActiveByEmployee(5L)).thenReturn(List.of());
		when(lineRepository.findByApproverId(5L)).thenReturn(List.of());

		var result = lifecycleService.resign(5L);

		assertThat(result.employeeId()).isEqualTo(5L);
	}
}
