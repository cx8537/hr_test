package com.example.hr.approval.entity;

import com.example.hr.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * 비품 신청서 본문(AP-044). 수령 거점(receiveLocationId) 필수. 행은 {@link AssetReqDocLine}로 분리.
 * 거점 모듈 도입 전이라 거점은 ID로만 보유(비활성 거점 선택 차단은 거점 도입 후 검증).
 */
@Entity
@Table(name = "asset_req_doc")
public class AssetReqDoc extends BaseEntity {

	@Column(name = "document_id", nullable = false, unique = true)
	private Long documentId;

	@Column(name = "desired_date")
	private LocalDate desiredDate;

	@Column(name = "receive_location_id", nullable = false)
	private Long receiveLocationId;

	private String reason;

	protected AssetReqDoc() {
	}

	public AssetReqDoc(Long documentId, LocalDate desiredDate, Long receiveLocationId,
			String reason) {
		this.documentId = documentId;
		this.desiredDate = desiredDate;
		this.receiveLocationId = receiveLocationId;
		this.reason = reason;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public LocalDate getDesiredDate() {
		return desiredDate;
	}

	public Long getReceiveLocationId() {
		return receiveLocationId;
	}

	public String getReason() {
		return reason;
	}
}
