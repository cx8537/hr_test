package com.example.hr.notification.entity;

import com.example.hr.common.entity.BaseEntity;
import com.example.hr.notification.domain.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 알림(NOTI). 시스템 내 알림함 레코드(외부 발송 없음 NOTI-001). 읽음/안읽음 상태 보유(NOTI-003).
 * 무기한 보관(NOTI-004).
 */
@Entity
@Table(name = "notification")
public class Notification extends BaseEntity {

	@Column(name = "recipient_id", nullable = false)
	private Long recipientId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;

	@Column(nullable = false)
	private String message;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	protected Notification() {
	}

	public Notification(Long recipientId, NotificationType type, String message) {
		this.recipientId = recipientId;
		this.type = type;
		this.message = message;
		this.read = false;
	}

	/** 읽음 처리(NOTI-003 AC2). */
	public void markRead() {
		this.read = true;
	}

	public Long getRecipientId() {
		return recipientId;
	}

	public NotificationType getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public boolean isRead() {
		return read;
	}
}
