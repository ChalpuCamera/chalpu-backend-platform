package com.example.chalpuplatform.notification.scheduler;

import com.example.chalpuplatform.notification.service.NotificationProcessor;
import com.example.chalpuplatform.notification.repository.NotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRecoveryScheduler {

    private static final int STUCK_THRESHOLD_MINUTES = 5;

    private final NotificationOutboxRepository outboxRepository;
    private final NotificationProcessor notificationProcessor;

    @Scheduled(fixedDelay = 60000)
    public void recoverStuckOutbox() {
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(STUCK_THRESHOLD_MINUTES);

        List<Long> stuckNotificationIds = outboxRepository
                .findDistinctNotificationIdsByIsProcessedFalseAndCreatedAtBefore(thresholdTime);

        if (!stuckNotificationIds.isEmpty()) {
            log.warn("백업 폴링: 미처리 알림 감지 - count={}, ids={}",
                stuckNotificationIds.size(), stuckNotificationIds);

            stuckNotificationIds.forEach(notificationId -> {
                try {
                    log.info("백업 폴링: 알림 재처리 시작 - notificationId={}", notificationId);
                    notificationProcessor.processNotification(notificationId);
                } catch (Exception e) {
                    log.error("백업 폴링: 알림 재처리 실패 - notificationId={}", notificationId, e);
                }
            });
        }
    }
}
