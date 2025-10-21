package com.example.chalpuplatform.subscription.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.common.exception.SubscriptionException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "store_subscriptions",
    indexes = {
        @Index(name = "idx_user_store", columnList = "user_id, store_id"),
        @Index(name = "idx_store_active", columnList = "store_id, is_active, notification_enabled")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_store",
        columnNames = {"user_id", "store_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StoreSubscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = true;

    public static StoreSubscription create(Long userId, Long storeId) {
        return StoreSubscription.builder()
                .userId(userId)
                .storeId(storeId)
                .isActive(true)
                .notificationEnabled(true)
                .build();
    }

    public void unsubscribe() {
        if (!this.isActive) {
            throw new SubscriptionException(ErrorMessage.SUBSCRIPTION_ALREADY_UNSUBSCRIBED);
        }
        this.isActive = false;
        this.notificationEnabled = false;
    }

    public void reactivate() {
        if (this.isActive) {
            throw new SubscriptionException(ErrorMessage.SUBSCRIPTION_ALREADY_ACTIVE);
        }
        this.isActive = true;
        this.notificationEnabled = true;
    }

    public void enableNotification() {
        if (!this.isActive) {
            throw new SubscriptionException(ErrorMessage.SUBSCRIPTION_NOT_ACTIVE);
        }
        if (this.notificationEnabled) {
            throw new SubscriptionException(ErrorMessage.NOTIFICATION_ALREADY_ENABLED);
        }
        this.notificationEnabled = true;
    }

    public void disableNotification() {
        if (!this.isActive) {
            throw new SubscriptionException(ErrorMessage.SUBSCRIPTION_NOT_ACTIVE);
        }
        if (!this.notificationEnabled) {
            throw new SubscriptionException(ErrorMessage.NOTIFICATION_ALREADY_DISABLED);
        }
        this.notificationEnabled = false;
    }

    public boolean canReceiveNotification() {
        return this.isActive && this.notificationEnabled;
    }
}
