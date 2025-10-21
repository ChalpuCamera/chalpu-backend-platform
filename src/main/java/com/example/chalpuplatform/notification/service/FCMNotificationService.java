package com.example.chalpuplatform.notification.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.NotificationException;
import com.example.chalpuplatform.fcm.domain.FCMToken;
import com.example.chalpuplatform.fcm.repository.FCMTokenRepository;
import com.example.chalpuplatform.notification.dto.NotificationRequest;
import com.example.chalpuplatform.notification.dto.NotificationResultDto;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMNotificationService {

    private static final int BATCH_SIZE = 500;

    private final FCMTokenRepository fcmTokenRepository;

    @Transactional
    public NotificationResultDto sendMulticastNotification(
            List<String> tokens,
            NotificationRequest request,
            List<FCMToken> tokenEntities) {

        try {
            List<List<String>> tokenChunks = chunkList(tokens, BATCH_SIZE);
            int totalSuccess = 0;
            int totalFailure = 0;
            List<String> invalidTokens = new ArrayList<>();
            List<String> successfulTokens = new ArrayList<>();

            for (List<String> chunk : tokenChunks) {
                MulticastMessage message = MulticastMessage.builder()
                        .setNotification(buildNotification(request))
                        .putAllData(request.getData())
                        .addAllTokens(chunk)
                        .setAndroidConfig(buildAndroidConfig(request))
                        .setApnsConfig(buildApnsConfig(request))
                        .build();

                BatchResponse batchResponse = FirebaseMessaging.getInstance()
                        .sendEachForMulticast(message);

                totalSuccess += batchResponse.getSuccessCount();
                totalFailure += batchResponse.getFailureCount();

                processBatchResponse(batchResponse, chunk, invalidTokens, successfulTokens);
            }

            updateSuccessfulTokensLastUsed(tokenEntities, invalidTokens);
            deactivateInvalidTokens(tokenEntities, invalidTokens);

            List<Long> successfulUserIds = extractSuccessfulUserIds(successfulTokens, tokenEntities);

            log.info("멀티캐스트 알림 발송 완료: 성공={}, 실패={}", totalSuccess, totalFailure);

            return NotificationResultDto.multipleResult(
                    totalSuccess,
                    totalFailure,
                    String.format("성공: %d, 실패: %d", totalSuccess, totalFailure),
                    successfulUserIds
            );

        } catch (FirebaseMessagingException e) {
            log.error("멀티캐스트 알림 발송 실패", e);
            throw new NotificationException(ErrorMessage.NOTIFICATION_MULTIPLE_SEND_FAILED);
        }
    }

    private Notification buildNotification(NotificationRequest request) {
        return Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();
    }

    private AndroidConfig buildAndroidConfig(NotificationRequest request) {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setChannelId("default")
                        .build())
                .build();
    }

    private ApnsConfig buildApnsConfig(NotificationRequest request) {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setSound("default")
                        .build())
                .build();
    }

    private void processBatchResponse(
            BatchResponse batchResponse,
            List<String> tokens,
            List<String> invalidTokens,
            List<String> successfulTokens) {

        List<SendResponse> responses = batchResponse.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            String token = tokens.get(i);

            if (response.isSuccessful()) {
                successfulTokens.add(token);
            } else {
                MessagingErrorCode errorCode = response.getException().getMessagingErrorCode();
                if (errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
                    errorCode == MessagingErrorCode.UNREGISTERED) {
                    invalidTokens.add(token);
                    log.debug("무효한 토큰 감지: token={}, error={}", token, errorCode);
                }
            }
        }
    }

    private void updateSuccessfulTokensLastUsed(List<FCMToken> tokenEntities, List<String> invalidTokens) {
        tokenEntities.stream()
                .filter(token -> !invalidTokens.contains(token.getFcmToken()))
                .forEach(FCMToken::updateLastUsed);
    }

    private void deactivateInvalidTokens(List<FCMToken> tokenEntities, List<String> invalidTokens) {
        if (invalidTokens.isEmpty()) {
            return;
        }

        tokenEntities.stream()
                .filter(token -> invalidTokens.contains(token.getFcmToken()))
                .forEach(FCMToken::deactivate);

        log.info("무효한 토큰 비활성화 완료: count={}", invalidTokens.size());
    }

    private List<Long> extractSuccessfulUserIds(List<String> successfulTokens, List<FCMToken> tokenEntities) {
        return tokenEntities.stream()
                .filter(token -> successfulTokens.contains(token.getFcmToken()))
                .map(token -> token.getUser().getId())
                .distinct()
                .collect(Collectors.toList());
    }

    private <T> List<List<T>> chunkList(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return chunks;
    }
}
