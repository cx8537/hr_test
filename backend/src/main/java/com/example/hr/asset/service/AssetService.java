package com.example.hr.asset.service;

import com.example.hr.asset.domain.AssetManagementType;
import com.example.hr.asset.domain.IndividualAssetStatus;
import com.example.hr.asset.domain.StockCalculator;
import com.example.hr.asset.domain.StockCalculator.StockEntry;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 비품 관리(AST-001~004/006). 개체/수량 분리. 결재 승인과 재고는 연동하지 않는다(AST-006).
 * 현재 수량은 입출고 이력에서 {@link StockCalculator}로 파생한다(AST-003 AC2).
 */
@Service
public class AssetService {

	private final AssetItemRepository itemRepository;
	private final IndividualAssetRepository individualRepository;
	private final QuantityAssetRepository quantityRepository;
	private final StockTransactionRepository stockRepository;
	private final Clock clock;

	public AssetService(AssetItemRepository itemRepository,
			IndividualAssetRepository individualRepository,
			QuantityAssetRepository quantityRepository,
			StockTransactionRepository stockRepository, Clock clock) {
		this.itemRepository = itemRepository;
		this.individualRepository = individualRepository;
		this.quantityRepository = quantityRepository;
		this.stockRepository = stockRepository;
		this.clock = clock;
	}

	/** 품목 등록(AST-001): 관리 유형 필수. 수량 유형이면 QuantityAsset도 함께 생성. */
	@Transactional
	public AssetItem registerItem(Long locationId, String name, AssetManagementType managementType) {
		if (managementType == null) {
			throw new IllegalArgumentException("관리 유형을 지정해야 합니다."); // AST-001 AC1
		}
		AssetItem item = itemRepository.save(new AssetItem(locationId, name, managementType));
		if (managementType == AssetManagementType.QUANTITY) {
			quantityRepository.save(new QuantityAsset(item.getId()));
		}
		return item;
	}

	/** 개체 등록(AST-002): 관리번호 중복 거부, 품목은 개체 유형이어야 함. */
	@Transactional
	public IndividualAsset registerIndividual(Long assetItemId, String assetNumber,
			IndividualAssetStatus status, LocalDate acquisitionDate) {
		AssetItem item = getItem(assetItemId);
		if (item.getManagementType() != AssetManagementType.INDIVIDUAL) {
			throw new IllegalArgumentException("개체 관리 품목이 아닙니다.");
		}
		if (individualRepository.existsByAssetNumber(assetNumber)) {
			throw new IllegalStateException("이미 존재하는 관리번호입니다: " + assetNumber); // AST-002 AC1
		}
		return individualRepository.save(
			new IndividualAsset(assetItemId, assetNumber, status, acquisitionDate));
	}

	/** 개체 상태 변경(AST-002 AC2: 현재 상태만 덮어쓰기). */
	@Transactional
	public IndividualAsset changeIndividualStatus(Long individualAssetId,
			IndividualAssetStatus status) {
		IndividualAsset asset = getIndividual(individualAssetId);
		asset.changeStatus(status);
		return asset;
	}

	/** 개체 폐기(AST-004: 레코드 보존, 상태만 DISCARDED). */
	@Transactional
	public IndividualAsset discardIndividual(Long individualAssetId) {
		IndividualAsset asset = getIndividual(individualAssetId);
		asset.discard();
		return asset;
	}

	/**
	 * 입출고 등록(AST-003 AC1): 이력 레코드 생성. 출고로 현재 수량이 음수가 되면 거부(AC3).
	 * 수량 관리 품목만 허용.
	 */
	@Transactional
	public StockTransaction recordStock(Long assetItemId, StockTransactionType type, int quantity) {
		AssetItem item = getItem(assetItemId);
		if (item.getManagementType() != AssetManagementType.QUANTITY) {
			throw new IllegalArgumentException("수량 관리 품목이 아닙니다.");
		}
		List<StockEntry> entries = Stream.concat(
			historyEntries(assetItemId).stream(),
			Stream.of(new StockEntry(type, quantity))).toList();
		StockCalculator.currentQuantity(entries); // 음수면 IllegalStateException(AC3)
		return stockRepository.save(
			new StockTransaction(assetItemId, type, quantity, OffsetDateTime.now(clock)));
	}

	/** 현재 수량 조회(AST-003 AC2): 입출고 이력에서 파생. */
	@Transactional(readOnly = true)
	public int currentQuantity(Long assetItemId) {
		return StockCalculator.currentQuantity(historyEntries(assetItemId));
	}

	/** 품목 소속 거점 ID(RBAC 범위 판정용). */
	@Transactional(readOnly = true)
	public Long locationIdOfItem(Long assetItemId) {
		return getItem(assetItemId).getLocationId();
	}

	/** 개체 소속 거점 ID(RBAC 범위 판정용). */
	@Transactional(readOnly = true)
	public Long locationIdOfIndividual(Long individualAssetId) {
		return getItem(getIndividual(individualAssetId).getAssetItemId()).getLocationId();
	}

	private List<StockEntry> historyEntries(Long assetItemId) {
		return stockRepository.findByAssetItemIdOrderByOccurredAtAsc(assetItemId).stream()
			.map(t -> new StockEntry(t.getType(), t.getQuantity()))
			.toList();
	}

	private AssetItem getItem(Long id) {
		return itemRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("비품 품목을 찾을 수 없습니다."));
	}

	private IndividualAsset getIndividual(Long id) {
		return individualRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("개체 비품을 찾을 수 없습니다."));
	}
}
