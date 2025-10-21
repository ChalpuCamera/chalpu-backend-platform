package com.example.chalpuplatform.notification.event;

import com.example.chalpuplatform.notification.service.NotificationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationProcessor notificationProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("notificationExecutor")
    public void onNotificationCreated(NotificationCreatedEvent event) {
        log.info("알림 생성 이벤트 수신: notificationId={}", event.getNotificationId());

        try {
            notificationProcessor.processNotification(event.getNotificationId());
        } catch (Exception e) {
            log.error("이벤트 처리 중 오류 발생: notificationId={}", event.getNotificationId(), e);
        }
    }
}
