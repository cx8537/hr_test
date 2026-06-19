package com.example.hr.asset.repository;

import com.example.hr.asset.entity.QuantityAsset;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuantityAssetRepository extends JpaRepository<QuantityAsset, Long> {

	Optional<QuantityAsset> findByAssetItemId(Long assetItemId);
}
