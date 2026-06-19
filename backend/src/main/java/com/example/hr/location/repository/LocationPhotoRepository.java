package com.example.hr.location.repository;

import com.example.hr.location.entity.LocationPhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationPhotoRepository extends JpaRepository<LocationPhoto, Long> {

	List<LocationPhoto> findByLocationId(Long locationId);
}
