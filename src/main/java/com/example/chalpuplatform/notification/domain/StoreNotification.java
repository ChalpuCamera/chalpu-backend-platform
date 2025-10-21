package com.example.chalpuplatform.notification.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.NotificationException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "store_notifications",
    indexes = {
        @Index(name = "idx_store_created", columnList = "store_id, created_at"),
        @Index(name = "idx_status_scheduled", columnList = "status, scheduled_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StoreNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "target_subscriber_count")
    private Integer targetSubscriberCount;

    @Column(name = "sent_count")
    @Builder.Default
    private Integer sentCount = 0;

    @Column(name = "failed_count")
    @Builder.Default
    private Integer failedCount = 0;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public static StoreNotification create(
            Long storeId,
            Long createdByUserId,
            NotificationType type,
            String title,
            String message,
            LocalDateTime scheduledAt,
            Integer targetSubscriberCount) {

        if (targetSubscriberCount == null || targetSubscriberCount <= 0) {
            throw new NotificationException(ErrorMessage.NO_SUBSCRIBERS);
        }

        return StoreNotification.builder()
                .storeId(storeId)
                .createdByUserId(createdByUserId)
                .type(type)
                .title(title)
                .message(message)
                .scheduledAt(scheduledAt)
                .status(NotificationStatus.PENDING)
                .targetSubscriberCount(targetSubscriberCount)
                .sentCount(0)
                .failedCount(0)
                .build();
    }

    public void markAsSending() {
        if (this.status != NotificationStatus.PENDING) {
            throw new NotificationException(ErrorMessage.NOTIFICATION_INVALID_STATUS);
        }
        this.status = NotificationStatus.SENDING;
    }

    public void incrementSentCount() {
        this.sentCount++;
        checkAndMarkAsCompleted();
    }

    public void incrementFailedCount() {
        this.failedCount++;
        checkAndMarkAsCompleted();
    }

    public void addCounts(int successCount, int failureCount) {
        this.sentCount += successCount;
        this.failedCount += failureCount;
        checkAndMarkAsCompleted();
    }

    private void checkAndMarkAsCompleted() {
        if (this.sentCount + this.failedCount >= this.targetSubscriberCount) {
            markAsCompleted();
        }
    }

    public void markAsCompleted() {
        this.status = NotificationStatus.COMPLETED;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
        this.sentAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == NotificationStatus.COMPLETED;
    }

    public boolean isPending() {
        return this.status == NotificationStatus.PENDING;
    }
}
