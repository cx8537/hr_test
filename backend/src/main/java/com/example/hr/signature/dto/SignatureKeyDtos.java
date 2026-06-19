package com.example.hr.signature.dto;

import com.example.hr.signature.domain.SignatureKeyStatus;
import com.example.hr.signature.entity.SignatureKey;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

/** 결재 공개키 DTO(FND-007/009). 서버는 공개키만 다룬다(개인키는 클라이언트 보관). */
public final class SignatureKeyDtos {

	private SignatureKeyDtos() {
	}

	public record IssueRequest(@NotBlank String publicKey) {
	}

	public record Response(Long id, Long employeeId, SignatureKeyStatus status,
			OffsetDateTime revokedAt) {
		public static Response from(SignatureKey k) {
			return new Response(k.getId(), k.getEmployeeId(), k.getStatus(), k.getRevokedAt());
		}
	}
}
