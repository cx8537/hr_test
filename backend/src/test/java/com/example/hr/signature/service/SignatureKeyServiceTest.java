package com.example.hr.signature.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.signature.domain.SignatureKeyStatus;
import com.example.hr.signature.entity.SignatureKey;
import com.example.hr.signature.repository.SignatureKeyRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** FND-007/009: 서명 키 발급·폐기·재발급. */
class SignatureKeyServiceTest {

	private SignatureKeyRepository repository;
	private SignatureKeyService service;

	@BeforeEach
	void setUp() {
		repository = mock(SignatureKeyRepository.class);
		service = new SignatureKeyService(repository,
			Clock.fixed(Instant.parse("2026-06-19T00:00:00Z"), ZoneOffset.UTC));
	}

	@Test
	void FND007_AC1_발급시_공개키만_저장_ACTIVE() {
		when(repository.save(any(SignatureKey.class))).thenAnswer(inv -> inv.getArgument(0));

		SignatureKey issued = service.issue(1L, "PUBKEY");

		assertThat(issued.getPublicKey()).isEqualTo("PUBKEY");
		assertThat(issued.getStatus()).isEqualTo(SignatureKeyStatus.ACTIVE);
		verify(repository).save(any(SignatureKey.class));
	}

	@Test
	void FND009_폐기시_삭제하지않고_REVOKED_보존() {
		SignatureKey key = new SignatureKey(1L, "PUBKEY");
		when(repository.findById(5L)).thenReturn(Optional.of(key));

		service.revoke(5L);

		assertThat(key.getStatus()).isEqualTo(SignatureKeyStatus.REVOKED);
		assertThat(key.getRevokedAt()).isNotNull();
		verify(repository, never()).delete(any());
		verify(repository, never()).deleteById(any());
	}

	@Test
	void FND009_재발급시_기존활성키_폐기_새키발급() {
		SignatureKey oldKey = new SignatureKey(1L, "OLD");
		when(repository.findByEmployeeIdAndStatus(1L, SignatureKeyStatus.ACTIVE))
			.thenReturn(List.of(oldKey));
		when(repository.save(any(SignatureKey.class))).thenAnswer(inv -> inv.getArgument(0));

		SignatureKey newKey = service.reissue(1L, "NEW");

		assertThat(oldKey.getStatus()).isEqualTo(SignatureKeyStatus.REVOKED); // 기존 폐기(보존)
		assertThat(newKey.getPublicKey()).isEqualTo("NEW");
		assertThat(newKey.getStatus()).isEqualTo(SignatureKeyStatus.ACTIVE);
	}
}
