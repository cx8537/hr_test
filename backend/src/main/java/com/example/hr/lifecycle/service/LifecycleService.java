package com.example.hr.lifecycle.service;

import com.example.hr.approval.domain.DocumentStatus;
import com.example.hr.approval.entity.ApprovalLineSnapshot;
import com.example.hr.approval.repository.ApprovalDocumentRepository;
import com.example.hr.approval.repository.ApprovalLineSnapshotRepository;
import com.example.hr.auth.domain.Role;
import com.example.hr.auth.domain.SysAdminGuard;
import com.example.hr.auth.entity.EmployeeRoleScope;
import com.example.hr.auth.repository.EmployeeRoleScopeRepository;
import com.example.hr.org.entity.Employee;
import com.example.hr.org.repository.EmployeeRepository;
import com.example.hr.signature.entity.SignatureKey;
import com.example.hr.signature.service.SignatureKeyService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 라이프사이클 처리(LIFE-A). 퇴사 처리는 한 트랜잭션에서 계정 비활성(A1) + 서명 키 자동 폐기(A3,
 * 과거 서명 검증용 보존) + 진행 중 결재 영향 산출(A2: 자동 우회 없음, 관리자 교체 안내용)을 함께 수행한다.
 * 마지막 시스템관리자 퇴사는 거부한다(A5).
 */
@Service
public class LifecycleService {

	/** 퇴사 처리 결과: 폐기된 키 수 + 결재선 교체가 필요한 진행 중 문서 ID(LIFE-A2 안내용). */
	public record ResignResult(Long employeeId, int revokedKeyCount,
			List<Long> affectedInProgressDocumentIds) {
	}

	private final EmployeeRepository employeeRepository;
	private final EmployeeRoleScopeRepository roleScopeRepository;
	private final SignatureKeyService signatureKeyService;
	private final ApprovalLineSnapshotRepository lineRepository;
	private final ApprovalDocumentRepository documentRepository;

	public LifecycleService(EmployeeRepository employeeRepository,
			EmployeeRoleScopeRepository roleScopeRepository,
			SignatureKeyService signatureKeyService,
			ApprovalLineSnapshotRepository lineRepository,
			ApprovalDocumentRepository documentRepository) {
		this.employeeRepository = employeeRepository;
		this.roleScopeRepository = roleScopeRepository;
		this.signatureKeyService = signatureKeyService;
		this.lineRepository = lineRepository;
		this.documentRepository = documentRepository;
	}

	@Transactional
	public ResignResult resign(Long employeeId) {
		// LIFE-A5: 마지막 시스템관리자 퇴사 거부(self-lockout 방지)
		Set<Long> sysAdminIds = roleScopeRepository.findByRole(Role.SYS_ADMIN).stream()
			.map(EmployeeRoleScope::getEmployeeId)
			.collect(Collectors.toSet());
		SysAdminGuard.requireCanRemove(sysAdminIds, employeeId);

		// LIFE-A1: 계정 비활성(로그인 차단, 과거 데이터 보존)
		Employee employee = employeeRepository.findById(employeeId)
			.orElseThrow(() -> new IllegalArgumentException("임직원을 찾을 수 없습니다."));
		employee.deactivate();

		// LIFE-A3: 활성 서명 키 자동 폐기(폐기 후에도 과거 서명 검증 가능 — FND-009)
		List<SignatureKey> activeKeys = signatureKeyService.findActiveByEmployee(employeeId);
		for (SignatureKey key : activeKeys) {
			signatureKeyService.revoke(key.getId());
		}

		// LIFE-A2: 진행 중 결재선에 걸린 문서 산출(자동 우회 없음 — 관리자 수동 교체 안내용)
		List<Long> affected = lineRepository.findByApproverId(employeeId).stream()
			.map(ApprovalLineSnapshot::getDocumentId)
			.distinct()
			.filter(this::isInProgress)
			.toList();

		return new ResignResult(employeeId, activeKeys.size(), affected);
	}

	private boolean isInProgress(Long documentId) {
		return documentRepository.findById(documentId)
			.map(d -> d.getStatus() == DocumentStatus.IN_PROGRESS)
			.orElse(false);
	}
}
