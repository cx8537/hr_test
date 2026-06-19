package com.example.hr.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** NOTI-002: 이벤트→알림(대상·유형·내용) 매핑. */
class NotificationFactoryTest {

	@Test
	void 결재차례_도달_대상은_결재자() {
		NotificationMessage m = NotificationFactory.approvalTurn(200L, "출장비");
		assertThat(m.recipientId()).isEqualTo(200L);
		assertThat(m.type()).isEqualTo(NotificationType.APPROVAL_TURN);
		assertThat(m.message()).contains("출장비");
	}

	@Test
	void 승인완료_대상은_상신자() {
		NotificationMessage m = NotificationFactory.approved(100L, "출장비");
		assertThat(m.recipientId()).isEqualTo(100L);
		assertThat(m.type()).isEqualTo(NotificationType.APPROVAL_APPROVED);
	}

	@Test
	void 반려_메시지에_사유_포함() {
		NotificationMessage m = NotificationFactory.rejected(100L, "출장비", "근거 부족");
		assertThat(m.type()).isEqualTo(NotificationType.APPROVAL_REJECTED);
		assertThat(m.message()).contains("근거 부족");
	}

	@Test
	void 합의요청_대상은_합의자() {
		NotificationMessage m = NotificationFactory.consentRequest(300L, "품의");
		assertThat(m.recipientId()).isEqualTo(300L);
		assertThat(m.type()).isEqualTo(NotificationType.CONSENT_REQUEST);
	}

	@Test
	void 보류_대상은_상신자() {
		NotificationMessage m = NotificationFactory.onHold(100L, "품의");
		assertThat(m.type()).isEqualTo(NotificationType.APPROVAL_ON_HOLD);
		assertThat(m.recipientId()).isEqualTo(100L);
	}

	@Test
	void 대결_대상은_대리인() {
		NotificationMessage m = NotificationFactory.deputyTurn(250L, "출장비");
		assertThat(m.type()).isEqualTo(NotificationType.DEPUTY_TURN);
		assertThat(m.recipientId()).isEqualTo(250L);
	}

	@Test
	void 예약_관리자취소_사유포함_대상은_예약자() {
		NotificationMessage m =
			NotificationFactory.reservationCancelledByAdmin(100L, "대회의실", "긴급 점검");
		assertThat(m.type()).isEqualTo(NotificationType.RESERVATION_CANCELLED_BY_ADMIN);
		assertThat(m.recipientId()).isEqualTo(100L);
		assertThat(m.message()).contains("긴급 점검");
	}

	@Test
	void 자원삭제_자동취소_대상은_예약자() {
		NotificationMessage m = NotificationFactory.reservationResourceRemoved(100L, "차량1");
		assertThat(m.type()).isEqualTo(NotificationType.RESERVATION_RESOURCE_REMOVED);
		assertThat(m.message()).contains("차량1");
	}
}
