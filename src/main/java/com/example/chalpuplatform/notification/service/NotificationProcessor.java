package com.example.chalpuplatform.notification.service;

import com.example.chalpuplatform.common.util.PagedIterator;
import com.example.chalpuplatform.fcm.domain.FCMToken;
import com.example.chalpuplatform.fcm.repository.FCMTokenRepository;
import com.example.chalpuplatform.notification.domain.NotificationOutbox;
import com.example.chalpuplatform.notification.domain.StoreNotification;
import com.example.chalpuplatform.notification.dto.NotificationRequest;
import com.example.chalpuplatform.notification.dto.NotificationResultDto;
import com.example.chalpuplatform.notification.repository.NotificationOutboxRepository;
import com.example.chalpuplatform.notification.repository.StoreNotificationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProcessor {

    private static final int BATCH_SIZE = 500;

    private final FCMNotificationService fcmNotificationService;
    private final NotificationOutboxRepository outboxRepository;
    private final StoreNotificationRepository notificationRepository;
    private final FCMTokenRepository fcmTokenRepository;
    private final ObjectMapper objectMapper;

    @Async("notificationExecutor")
    @Transactional
    public void processNotification(Long notificationId) {
        log.info("알림 처리 시작: notificationId={}", notificationId);

        StoreNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        notification.markAsSending();

        PagedIterator<NotificationOutbox> iterator = new PagedIterator<>(
                pageable -> outboxRepository.findByNotificationIdAndIsProcessedFalse(
                        notificationId, pageable
                )
        );

        int processedBatches = 0;

        while (iterator.hasNext()) {
            List<NotificationOutbox> batch = new ArrayList<>();

            while (batch.size() < BATCH_SIZE && iterator.hasNext()) {
                batch.add(iterator.next());
            }

            processBatch(notificationId, batch);

            processedBatches++;
            log.info("배치 {} 처리 완료: notificationId={}, count={}",
                processedBatches, notificationId, batch.size());
        }

        log.info("알림 처리 완료: notificationId={}, totalBatches={}",
            notificationId, processedBatches);
    }

    private void processBatch(Long notificationId, List<NotificationOutbox> batch) {
        if (batch.isEmpty()) {
            return;
        }

        try {
            List<String> tokens = batch.stream()
                    .map(NotificationOutbox::getFcmToken)
                    .collect(Collectors.toList());

            List<Long> userIds = batch.stream()
                    .map(NotificationOutbox::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            List<FCMToken> fcmTokens = fcmTokenRepository.findByUserIdInAndIsAllowedTrue(userIds);

            NotificationOutbox sample = batch.get(0);
            NotificationRequest request = NotificationRequest.builder()
                    .title(sample.getTitle())
                    .body(sample.getMessage())
                    .data(parseData(sample.getData()))
                    .build();

            NotificationResultDto result = fcmNotificationService.sendMulticastNotification(
                    tokens, request, fcmTokens
            );

            updateOutboxByResult(batch, result);
            updateNotificationCounts(notificationId, result);

        } catch (Exception e) {
            log.error("배치 처리 실패: notificationId={}", notificationId, e);
            markBatchAsFailed(batch, e.getMessage());
        }
    }

    @Transactional
    protected void updateOutboxByResult(List<NotificationOutbox> batch, NotificationResultDto result) {
        List<Long> successfulUserIds = result.getSuccessfulUserIds();

        batch.forEach(outbox -> {
            if (successfulUserIds.contains(outbox.getUserId())) {
                outbox.markAsProcessed();
            } else {
                if (outbox.canRetry()) {
                    outbox.incrementRetry("FCM 발송 실패");
                } else {
                    outbox.markAsFailed("최대 재시도 횟수 초과");
                }
            }
        });

        outboxRepository.saveAll(batch);
    }

    @Transactional
    protected void updateNotificationCounts(Long notificationId, NotificationResultDto result) {
        StoreNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));

        notification.addCounts(result.getSuccessCount(), result.getFailureCount());

        notificationRepository.save(notification);

        log.debug("알림 카운트 업데이트: notificationId={}, sent={}, failed={}",
            notificationId, notification.getSentCount(), notification.getFailedCount());
    }

    @Transactional
    protected void markBatchAsFailed(List<NotificationOutbox> batch, String errorMessage) {
        batch.forEach(outbox -> {
            if (outbox.canRetry()) {
                outbox.incrementRetry(errorMessage);
            } else {
                outbox.markAsFailed(errorMessage);
            }
        });

        outboxRepository.saveAll(batch);
    }

    private Map<String, String> parseData(String dataJson) {
        if (dataJson == null || dataJson.trim().isEmpty()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(dataJson, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("데이터 JSON 파싱 실패: {}", dataJson, e);
            return Map.of();
        }
    }
}
