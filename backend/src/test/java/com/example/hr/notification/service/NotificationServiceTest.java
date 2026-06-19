package com.example.hr.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.hr.notification.domain.NotificationFactory;
import com.example.hr.notification.entity.Notification;
import com.example.hr.notification.repository.NotificationRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/** NOTI-001/003: 알림 생성·안읽음 수·읽음 처리. */
class NotificationServiceTest {

	private NotificationRepository notificationRepository;
	private NotificationService notificationService;

	@BeforeEach
	void setUp() {
		notificationRepository = mock(NotificationRepository.class);
		notificationService = new NotificationService(notificationRepository);
	}

	@Test
	void NOTI001_알림_생성() {
		when(notificationRepository.save(any(Notification.class)))
			.thenAnswer(inv -> inv.getArgument(0));

		Notification n = notificationService.create(
			NotificationFactory.approvalTurn(200L, "출장비"));

		assertThat(n.getRecipientId()).isEqualTo(200L);
		assertThat(n.isRead()).isFalse();
	}

	@Test
	void NOTI003_AC1_안읽음_수() {
		when(notificationRepository.countByRecipientIdAndReadFalse(200L)).thenReturn(3L);

		assertThat(notificationService.unreadCount(200L)).isEqualTo(3L);
	}

	@Test
	void NOTI003_AC2_읽음_처리() {
		Notification n = new Notification(200L, null, "msg");
		when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

		notificationService.markRead(5L, 200L);

		assertThat(n.isRead()).isTrue();
	}

	@Test
	void 타인_알림_읽음처리_거부() {
		Notification n = new Notification(200L, null, "msg");
		when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

		assertThatThrownBy(() -> notificationService.markRead(5L, 999L))
			.isInstanceOf(AccessDeniedException.class);
		assertThat(n.isRead()).isFalse();
	}
}
