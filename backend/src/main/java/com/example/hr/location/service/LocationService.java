package com.example.hr.location.service;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.location.domain.LocationDeactivation;
import com.example.hr.location.domain.LocationDeactivation.Decision;
import com.example.hr.location.entity.Location;
import com.example.hr.location.repository.LocationRepository;
import com.example.hr.location.service.LocationResourceCounter.Counts;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 거점 관리(LOC-001/002/003/006). 코드 중복 거부, 좌표·담당자 지정, 비활성화는 자원 잔존 검사 후 소프트삭제.
 */
@Service
public class LocationService {

	private final LocationRepository locationRepository;
	private final LocationResourceCounter resourceCounter;

	public LocationService(LocationRepository locationRepository,
			LocationResourceCounter resourceCounter) {
		this.locationRepository = locationRepository;
		this.resourceCounter = resourceCounter;
	}

	@Transactional
	public Location create(String locationCode, String name, String address, String locationType,
			Long managerId) {
		if (locationRepository.existsByLocationCode(locationCode)) {
			throw new IllegalStateException("이미 존재하는 거점코드입니다: " + locationCode); // LOC-001 AC1
		}
		return locationRepository.save(
			new Location(locationCode, name, address, locationType, managerId));
	}

	@Transactional
	public Location update(Long id, String name, String address, String contact, String fax,
			String locationType) {
		Location location = get(id);
		location.update(name, address, contact, fax, locationType);
		return location;
	}

	/** 좌표 지정(LOC-002: 지도 핀으로 받은 위/경도 저장). */
	@Transactional
	public Location updateCoordinates(Long id, BigDecimal latitude, BigDecimal longitude) {
		Location location = get(id);
		location.updateCoordinates(latitude, longitude);
		return location;
	}

	/** 담당자 지정(LOC-001 AC2 / LOC-003: 권한과 별개). */
	@Transactional
	public Location assignManager(Long id, Long employeeId) {
		Location location = get(id);
		location.assignManager(employeeId);
		return location;
	}

	/**
	 * 비활성화(LOC-006/LIFE-C1): 소속 활성 자원이 남아 있으면 거부, 없으면 소프트삭제.
	 * 영향 건수는 {@link LocationResourceCounter}로 조회해 {@link LocationDeactivation}이 판정한다.
	 */
	@Transactional
	public Decision deactivate(Long id) {
		Location location = get(id);
		Counts counts = resourceCounter.countFor(id);
		Decision decision = LocationDeactivation.evaluate(
			counts.activeResources(), counts.futureReservations());
		if (!decision.allowed()) {
			throw new IllegalStateException(decision.reason()); // LOC-006 AC1
		}
		location.deactivate();
		return decision;
	}

	@Transactional(readOnly = true)
	public List<Location> findActive() {
		return locationRepository.findByStatus(EntityStatus.ACTIVE); // LOC-005/006 AC2
	}

	private Location get(Long id) {
		return locationRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("거점을 찾을 수 없습니다."));
	}
}
