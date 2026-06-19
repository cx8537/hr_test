package com.example.hr.asset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hr.asset.domain.AssetManagementType;
import com.example.hr.asset.domain.IndividualAssetStatus;
import com.example.hr.asset.domain.StockTransactionType;
import com.example.hr.asset.entity.AssetItem;
import com.example.hr.asset.entity.IndividualAsset;
import com.example.hr.asset.entity.QuantityAsset;
import com.example.hr.asset.entity.StockTransaction;
import com.example.hr.asset.repository.AssetItemRepository;
import com.example.hr.asset.repository.IndividualAssetRepository;
import com.example.hr.asset.repository.QuantityAssetRepository;
import com.example.hr.asset.repository.StockTransactionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** AST-001~004: 비품 서비스(유형 필수·관리번호 중복·입출고 현재수량·출고 음수 거부·폐기 보존). */
class AssetServiceTest {

	private AssetItemRepository itemRepository;
	private IndividualAssetRepository individualRepository;
	private QuantityAssetRepository quantityRepository;
	private StockTransactionRepository stockRepository;
	private AssetService assetService;

	@BeforeEach
	void setUp() {
		itemRepository = mock(AssetItemRepository.class);
		individualRepository = mock(IndividualAssetRepository.class);
		quantityRepository = mock(QuantityAssetRepository.class);
		stockRepository = mock(StockTransactionRepository.class);
		Clock clock = Clock.fixed(Instant.parse("2026-06-19T00:00:00Z"), ZoneOffset.UTC);
		assetService = new AssetService(itemRepository, individualRepository, quantityRepository,
			stockRepository, clock);
	}

	private AssetItem item(AssetManagementType type) {
		return new AssetItem(9L, "노트북", type);
	}

	@Test
	void AST001_AC1_관리유형_필수() {
		assertThatThrownBy(() -> assetService.registerItem(9L, "노트북", null))
			.isInstanceOf(IllegalArgumentException.class);
		verify(itemRepository, never()).save(any());
	}

	@Test
	void AST003_수량유형_등록시_QuantityAsset_생성() {
		when(itemRepository.save(any(AssetItem.class))).thenAnswer(inv -> inv.getArgument(0));

		assetService.registerItem(9L, "A4용지", AssetManagementType.QUANTITY);

		verify(quantityRepository).save(any(QuantityAsset.class));
	}

	@Test
	void AST002_AC1_관리번호_중복_거부() {
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item(AssetManagementType.INDIVIDUAL)));
		when(individualRepository.existsByAssetNumber("NB-001")).thenReturn(true);

		assertThatThrownBy(() -> assetService.registerIndividual(1L, "NB-001",
			IndividualAssetStatus.USING, null))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void AST004_폐기는_레코드보존_상태만_DISCARDED() {
		IndividualAsset asset = new IndividualAsset(1L, "NB-001", IndividualAssetStatus.USING, null);
		when(individualRepository.findById(5L)).thenReturn(Optional.of(asset));

		assetService.discardIndividual(5L);

		assertThat(asset.getStatus()).isEqualTo(IndividualAssetStatus.DISCARDED);
		verify(individualRepository, never()).delete(any()); // 삭제 금지
	}

	@Test
	void AST003_AC2_입출고_현재수량_파생() {
		when(stockRepository.findByAssetItemIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
			new StockTransaction(1L, StockTransactionType.IN, 10, null),
			new StockTransaction(1L, StockTransactionType.OUT, 3, null)));

		assertThat(assetService.currentQuantity(1L)).isEqualTo(7);
	}

	@Test
	void AST003_AC3_출고로_음수면_거부() {
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item(AssetManagementType.QUANTITY)));
		when(stockRepository.findByAssetItemIdOrderByOccurredAtAsc(1L)).thenReturn(List.of(
			new StockTransaction(1L, StockTransactionType.IN, 2, null)));

		assertThatThrownBy(() -> assetService.recordStock(1L, StockTransactionType.OUT, 5))
			.isInstanceOf(IllegalStateException.class);
		verify(stockRepository, never()).save(any());
	}

	@Test
	void AST003_정상_입고_저장() {
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item(AssetManagementType.QUANTITY)));
		when(stockRepository.findByAssetItemIdOrderByOccurredAtAsc(1L)).thenReturn(List.of());
		when(stockRepository.save(any(StockTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

		StockTransaction tx = assetService.recordStock(1L, StockTransactionType.IN, 10);

		assertThat(tx.getQuantity()).isEqualTo(10);
		verify(stockRepository).save(any(StockTransaction.class));
	}
}
