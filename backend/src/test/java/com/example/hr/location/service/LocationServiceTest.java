package com.example.hr.location.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.location.entity.Location;
import com.example.hr.location.repository.LocationRepository;
import com.example.hr.location.service.LocationResourceCounter.Counts;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** LOC-001/002/006: 거점 생성·좌표·비활성화(자원 잔존 거부) 서비스. */
class LocationServiceTest {

	private LocationRepository locationRepository;
	private LocationResourceCounter resourceCounter;
	private LocationService locationService;

	@BeforeEach
	void setUp() {
		locationRepository = mock(LocationRepository.class);
		resourceCounter = mock(LocationResourceCounter.class);
		locationService = new LocationService(locationRepository, resourceCounter);
	}

	@Test
	void LOC001_거점_생성() {
		when(locationRepository.existsByLocationCode("HQ")).thenReturn(false);
		when(locationRepository.save(any(Location.class))).thenAnswer(inv -> inv.getArgument(0));

		Location loc = locationService.create("HQ", "본사", "서울", "HQ", null);

		assertThat(loc.getLocationCode()).isEqualTo("HQ");
		assertThat(loc.getStatus()).isEqualTo(EntityStatus.ACTIVE);
	}

	@Test
	void LOC001_AC1_코드중복_거부() {
		when(locationRepository.existsByLocationCode("HQ")).thenReturn(true);

		assertThatThrownBy(() -> locationService.create("HQ", "본사", "서울", "HQ", null))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void LOC002_AC1_좌표_저장() {
		Location loc = new Location("HQ", "본사", "서울", "HQ", null);
		when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));

		locationService.updateCoordinates(1L, new BigDecimal("37.5665"), new BigDecimal("126.9780"));

		assertThat(loc.getLatitude()).isEqualByComparingTo("37.5665");
		assertThat(loc.getLongitude()).isEqualByComparingTo("126.9780");
	}

	@Test
	void LOC003_담당자_지정() {
		Location loc = new Location("HQ", "본사", "서울", "HQ", null);
		when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));

		locationService.assignManager(1L, 42L);

		assertThat(loc.getManagerId()).isEqualTo(42L);
	}

	@Test
	void LOC006_AC1_자원잔존시_비활성화_거부() {
		Location loc = new Location("HQ", "본사", "서울", "HQ", null);
		when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));
		when(resourceCounter.countFor(1L)).thenReturn(new Counts(2, 0));

		assertThatThrownBy(() -> locationService.deactivate(1L))
			.isInstanceOf(IllegalStateException.class);
		assertThat(loc.getStatus()).isEqualTo(EntityStatus.ACTIVE); // 거부되어 활성 유지
	}

	@Test
	void LOC006_자원없으면_비활성화_허용() {
		Location loc = new Location("HQ", "본사", "서울", "HQ", null);
		when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));
		when(resourceCounter.countFor(1L)).thenReturn(new Counts(0, 3));

		var decision = locationService.deactivate(1L);

		assertThat(decision.allowed()).isTrue();
		assertThat(decision.futureReservationCount()).isEqualTo(3); // 경고용 영향 건수
		assertThat(loc.getStatus()).isEqualTo(EntityStatus.INACTIVE);
	}
}
