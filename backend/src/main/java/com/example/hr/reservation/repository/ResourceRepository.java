package com.example.hr.reservation.repository;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.reservation.entity.Resource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

	List<Resource> findByLocationId(Long locationId);

	List<Resource> findByStatus(EntityStatus status);
}
