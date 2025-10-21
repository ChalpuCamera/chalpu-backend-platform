package com.example.chalpuplatform.notification.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "notification_outbox",
    indexes = {
        @Index(name = "idx_processed_created", columnList = "is_processed, created_at"),
        @Index(name = "idx_notification_id", columnList = "notification_id"),
        @Index(name = "idx_user_notification", columnList = "user_id, notification_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class NotificationOutbox extends BaseTimeEntity {

    private static final Integer MAX_RETRY = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "data", columnDefinition = "TEXT")
    private String data;

    @Column(name = "is_processed", nullable = false)
    @Builder.Default
    private Boolean isProcessed = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public static NotificationOutbox create(
            Long notificationId,
            Long userId,
            String fcmToken,
            String title,
            String message,
            String data) {

        return NotificationOutbox.builder()
                .notificationId(notificationId)
                .userId(userId)
                .fcmToken(fcmToken)
                .title(title)
                .message(message)
                .data(data)
                .isProcessed(false)
                .retryCount(0)
                .build();
    }

    public void markAsProcessed() {
        this.isProcessed = true;
        this.processedAt = LocalDateTime.now();
    }

    public void incrementRetry(String error) {
        this.retryCount++;
        this.errorMessage = error;
    }

    public boolean canRetry() {
        return this.retryCount < MAX_RETRY;
    }

    public void markAsFailed(String error) {
        this.isProcessed = true;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = error;
    }
}
