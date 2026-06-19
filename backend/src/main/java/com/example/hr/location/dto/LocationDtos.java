package com.example.hr.location.dto;

import com.example.hr.common.domain.EntityStatus;
import com.example.hr.location.entity.Location;
import com.example.hr.location.entity.LocationPhoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 거점 관리 DTO(LOC-001/002/003/004). */
public final class LocationDtos {

	private LocationDtos() {
	}

	public record CreateRequest(@NotBlank String locationCode, @NotBlank String name,
			String address, String locationType, Long managerId) {
	}

	public record UpdateRequest(@NotBlank String name, String address, String contact, String fax,
			String locationType) {
	}

	public record CoordinatesRequest(@NotNull BigDecimal latitude, @NotNull BigDecimal longitude) {
	}

	public record ManagerRequest(@NotNull Long managerId) {
	}

	public record Response(Long id, String locationCode, String name, String address,
			BigDecimal latitude, BigDecimal longitude, String contact, String fax,
			String locationType, Long managerId, EntityStatus status) {
		public static Response from(Location l) {
			return new Response(l.getId(), l.getLocationCode(), l.getName(), l.getAddress(),
				l.getLatitude(), l.getLongitude(), l.getContact(), l.getFax(), l.getLocationType(),
				l.getManagerId(), l.getStatus());
		}
	}

	public record PhotoResponse(Long id, Long locationId, String fileName, String contentType,
			long sizeBytes) {
		public static PhotoResponse from(LocationPhoto p) {
			return new PhotoResponse(p.getId(), p.getLocationId(), p.getFileName(),
				p.getContentType(), p.getSizeBytes());
		}
	}
}
