package com.example.hr.reservation.repository;

import com.example.hr.reservation.entity.ResourcePhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourcePhotoRepository extends JpaRepository<ResourcePhoto, Long> {

	List<ResourcePhoto> findByResourceId(Long resourceId);
}
