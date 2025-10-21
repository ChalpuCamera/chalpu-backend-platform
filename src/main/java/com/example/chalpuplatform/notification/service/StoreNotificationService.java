package com.example.chalpuplatform.notification.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.NotificationException;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.fcm.domain.FCMToken;
import com.example.chalpuplatform.fcm.repository.FCMTokenRepository;
import com.example.chalpuplatform.notification.domain.NotificationOutbox;
import com.example.chalpuplatform.notification.domain.StoreNotification;
import com.example.chalpuplatform.notification.dto.CreateNotificationRequest;
import com.example.chalpuplatform.notification.dto.CreateNotificationResponse;
import com.example.chalpuplatform.notification.event.NotificationCreatedEvent;
import com.example.chalpuplatform.notification.repository.NotificationOutboxRepository;
import com.example.chalpuplatform.notification.repository.StoreNotificationRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.subscription.domain.StoreSubscription;
import com.example.chalpuplatform.subscription.repository.StoreSubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StoreNotificationService {

    private final StoreNotificationRepository notificationRepository;
    private final NotificationOutboxRepository outboxRepository;
    private final StoreSubscriptionRepository subscriptionRepository;
    private final FCMTokenRepository fcmTokenRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public CreateNotificationResponse createNotification(Long userId, CreateNotificationRequest request) {
        validateOwnerPermission(userId, request.getStoreId());

        List<StoreSubscription> subscriptions = subscriptionRepository
                .findActiveSubscribersByStoreId(request.getStoreId());

        if (subscriptions.isEmpty()) {
            throw new NotificationException(ErrorMessage.NO_SUBSCRIBERS);
        }

        List<Long> subscriberIds = subscriptions.stream()
                .map(StoreSubscription::getUserId)
                .collect(Collectors.toList());

        List<FCMToken> fcmTokens = fcmTokenRepository.findByUserIdInAndIsAllowedTrue(subscriberIds);

        if (fcmTokens.isEmpty()) {
            throw new NotificationException(ErrorMessage.NO_FCM_TOKENS);
        }

        Map<Long, String> userIdToTokenMap = fcmTokens.stream()
                .collect(Collectors.toMap(
                        token -> token.getUser().getId(),
                        FCMToken::getFcmToken,
                        (existing, replacement) -> existing
                ));

        StoreNotification notification = StoreNotification.create(
                request.getStoreId(),
                userId,
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                request.getScheduledAt(),
                userIdToTokenMap.size()
        );

        notificationRepository.save(notification);

        String dataJson = convertDataToJson(request.getData());

        List<NotificationOutbox> outboxList = subscriptions.stream()
                .filter(sub -> userIdToTokenMap.containsKey(sub.getUserId()))
                .map(sub -> NotificationOutbox.create(
                        notification.getId(),
                        sub.getUserId(),
                        userIdToTokenMap.get(sub.getUserId()),
                        request.getTitle(),
                        request.getMessage(),
                        dataJson
                ))
                .collect(Collectors.toList());

        outboxRepository.saveAll(outboxList);

        log.info("알림 생성 완료: notificationId={}, storeId={}, targetCount={}",
                notification.getId(), request.getStoreId(), userIdToTokenMap.size());

        eventPublisher.publishEvent(new NotificationCreatedEvent(notification.getId()));

        return CreateNotificationResponse.of(notification.getId(), userIdToTokenMap.size());
    }

    private void validateOwnerPermission(Long userId, Long storeId) {
        userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, storeId)
                .orElseThrow(() -> new StoreException(ErrorMessage.STORE_PERMISSION_DENIED));
    }

    private String convertDataToJson(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("데이터 JSON 변환 실패", e);
            return null;
        }
    }
}
