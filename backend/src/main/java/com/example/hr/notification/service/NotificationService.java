package com.example.hr.notification.service;

import com.example.hr.notification.domain.NotificationMessage;
import com.example.hr.notification.entity.Notification;
import com.example.hr.notification.repository.NotificationRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 생성·조회·읽음 처리(NOTI). 다른 모듈은 {@code NotificationFactory}로 만든
 * {@link NotificationMessage}를 {@link #create}로 발행한다(외부 발송 없음, NOTI-001). 무기한 보관(NOTI-004).
 */
@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	/** 알림 1건 생성(알림함 레코드만, 외부 발송 없음 NOTI-001 AC1). */
	@Transactional
	public Notification create(NotificationMessage message) {
		return notificationRepository.save(
			new Notification(message.recipientId(), message.type(), message.message()));
	}

	@Transactional(readOnly = true)
	public List<Notification> list(Long recipientId) {
		return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
	}

	/** 안읽음 수(NOTI-003 AC1: 뱃지 폴링용). */
	@Transactional(readOnly = true)
	public long unreadCount(Long recipientId) {
		return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
	}

	/** 읽음 처리(NOTI-003 AC2). 본인 알림만 가능. */
	@Transactional
	public void markRead(Long notificationId, Long requesterId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
		if (!notification.getRecipientId().equals(requesterId)) {
			throw new AccessDeniedException("본인 알림만 읽음 처리할 수 있습니다.");
		}
		notification.markRead();
	}
}
