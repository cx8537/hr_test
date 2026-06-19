package com.example.hr.asset.repository;

import com.example.hr.asset.entity.StockTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

	/** 현재 수량 산출용 이력(발생 시각 순). */
	List<StockTransaction> findByAssetItemIdOrderByOccurredAtAsc(Long assetItemId);
}
