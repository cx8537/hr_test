package com.example.hr.reservation.dto;

import com.example.hr.reservation.domain.ReservationStatus;
import com.example.hr.reservation.domain.ResourceType;
import com.example.hr.reservation.entity.Reservation;
import com.example.hr.reservation.entity.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/** 예약·자원 DTO(RSV-001~005). */
public final class ReservationDtos {

	private ReservationDtos() {
	}

	public record CreateResourceRequest(@NotNull Long locationId, @NotNull ResourceType type,
			@NotBlank String name) {
	}

	public record ResourceResponse(Long id, Long locationId, ResourceType type, String name,
			String status) {
		public static ResourceResponse from(Resource r) {
			return new ResourceResponse(r.getId(), r.getLocationId(), r.getType(), r.getName(),
				r.getStatus().name());
		}
	}

	public record ReserveRequest(@NotNull Long resourceId, @NotNull OffsetDateTime startAt,
			@NotNull OffsetDateTime endAt, String purpose, int headcount, String note,
			String destination, String driver) {
	}

	public record CancelRequest(String reason) {
	}

	public record ReservationResponse(Long id, Long resourceId, Long reserverId,
			OffsetDateTime startAt, OffsetDateTime endAt, String purpose, int headcount,
			String destination, String driver, ReservationStatus status) {
		public static ReservationResponse from(Reservation r) {
			return new ReservationResponse(r.getId(), r.getResourceId(), r.getReserverId(),
				r.getStartAt(), r.getEndAt(), r.getPurpose(), r.getHeadcount(),
				r.getDestination(), r.getDriver(), r.getStatus());
		}
	}
}
