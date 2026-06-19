package com.example.hr.asset.repository;

import com.example.hr.asset.entity.AssetItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetItemRepository extends JpaRepository<AssetItem, Long> {

	List<AssetItem> findByLocationId(Long locationId);
}
