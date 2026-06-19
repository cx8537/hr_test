package com.example.hr.asset.repository;

import com.example.hr.asset.domain.AssetPhotoOwnerType;
import com.example.hr.asset.entity.AssetPhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetPhotoRepository extends JpaRepository<AssetPhoto, Long> {

	List<AssetPhoto> findByOwnerTypeAndOwnerId(AssetPhotoOwnerType ownerType, Long ownerId);
}
