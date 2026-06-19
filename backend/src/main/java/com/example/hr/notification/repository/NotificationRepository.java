package com.example.hr.notification.repository;

import com.example.hr.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	/** 수신자 알림 최신순(NOTI-003). */
	List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

	/** 안읽음 수(NOTI-003 AC1). */
	long countByRecipientIdAndReadFalse(Long recipientId);
}
