package com.example.hr.notification;

import com.example.hr.notification.dto.NotificationDtos.Response;
import com.example.hr.notification.dto.NotificationDtos.UnreadCountResponse;
import com.example.hr.notification.service.NotificationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 API(NOTI-003). 본인 알림함 조회·안읽음 수 폴링·읽음 처리. 본인 것만 접근(서비스에서 강제).
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	/** 본인 알림 목록(최신순). */
	@GetMapping
	public List<Response> list(@AuthenticationPrincipal Long actorId) {
		return notificationService.list(actorId).stream().map(Response::from).toList();
	}

	/** 안읽음 수(뱃지 폴링, NOTI-003 AC1). */
	@GetMapping("/unread-count")
	public UnreadCountResponse unreadCount(@AuthenticationPrincipal Long actorId) {
		return new UnreadCountResponse(notificationService.unreadCount(actorId));
	}

	/** 읽음 처리(NOTI-003 AC2). */
	@PostMapping("/{id}/read")
	public ResponseEntity<Void> markRead(@AuthenticationPrincipal Long actorId,
			@PathVariable Long id) {
		notificationService.markRead(id, actorId);
		return ResponseEntity.noContent().build();
	}
}
