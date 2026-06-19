package com.example.hr.asset.repository;

import com.example.hr.asset.entity.IndividualAsset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndividualAssetRepository extends JpaRepository<IndividualAsset, Long> {

	boolean existsByAssetNumber(String assetNumber);

	List<IndividualAsset> findByAssetItemId(Long assetItemId);
}
