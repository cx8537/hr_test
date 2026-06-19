package com.example.hr.notification.dto;

import com.example.hr.notification.domain.NotificationType;
import com.example.hr.notification.entity.Notification;
import java.time.OffsetDateTime;

/** 알림 DTO(NOTI-003). */
public final class NotificationDtos {

	private NotificationDtos() {
	}

	public record Response(Long id, NotificationType type, String message, boolean read,
			OffsetDateTime createdAt) {
		public static Response from(Notification n) {
			return new Response(n.getId(), n.getType(), n.getMessage(), n.isRead(),
				n.getCreatedAt());
		}
	}

	public record UnreadCountResponse(long unread) {
	}
}
