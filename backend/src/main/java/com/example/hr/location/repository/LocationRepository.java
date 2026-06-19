package com.example.hr.location.repository;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.location.entity.Location;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

	boolean existsByLocationCode(String locationCode);

	/** 활성 거점만 조회(LOC-005/006 AC2: 신규 참조·지도 표시 대상). */
	List<Location> findByStatus(EntityStatus status);
}
