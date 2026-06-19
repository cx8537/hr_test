package com.example.hr.notification.domain;

/** 생성될 알림 1건(수신자, 유형, 메시지). 순수 값 객체 — 영속화는 서비스가 담당. */
public record NotificationMessage(Long recipientId, NotificationType type, String message) {
}
