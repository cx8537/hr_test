package com.example.hr.asset.service;

import com.example.hr.asset.domain.AssetManagementType;
import com.example.hr.asset.domain.IndividualAssetStatus;
import com.example.hr.asset.domain.StockCalculator;
import com.example.hr.asset.domain.StockCalculator.StockEntry;
import com.example.hr.asset.entity.AssetItem;
import com.example.hr.asset.repository.AssetItemRepository;
import com.example.hr.asset.repository.IndividualAssetRepository;
import com.example.hr.asset.repository.StockTransactionRepository;
import com.example.hr.location.service.LocationResourceCounter;
import org.springframework.stereotype.Component;

/**
 * 거점 비활성화 판정용 자원 카운터(LIFE-C1) — 비품 기준 구현. 기본 0건 카운터를 대체한다.
 * 활성 자원 = 폐기되지 않은 개체 수 + 수량 품목의 현재 수량 합. 미래 예약은 RSV 모듈 도입 후 합산(현재 0).
 */
@Component
public class AssetResourceCounter implements LocationResourceCounter {

	private final AssetItemRepository itemRepository;
	private final IndividualAssetRepository individualRepository;
	private final StockTransactionRepository stockRepository;

	public AssetResourceCounter(AssetItemRepository itemRepository,
			IndividualAssetRepository individualRepository,
			StockTransactionRepository stockRepository) {
		this.itemRepository = itemRepository;
		this.individualRepository = individualRepository;
		this.stockRepository = stockRepository;
	}

	@Override
	public Counts countFor(Long locationId) {
		int activeResources = 0;
		for (AssetItem item : itemRepository.findByLocationId(locationId)) {
			if (item.getManagementType() == AssetManagementType.INDIVIDUAL) {
				activeResources += (int) individualRepository.findByAssetItemId(item.getId()).stream()
					.filter(a -> a.getStatus() != IndividualAssetStatus.DISCARDED)
					.count();
			} else {
				activeResources += currentQuantity(item.getId());
			}
		}
		return new Counts(activeResources, 0);
	}

	private int currentQuantity(Long assetItemId) {
		return StockCalculator.currentQuantity(
			stockRepository.findByAssetItemIdOrderByOccurredAtAsc(assetItemId).stream()
				.map(t -> new StockEntry(t.getType(), t.getQuantity()))
				.toList());
	}
}
