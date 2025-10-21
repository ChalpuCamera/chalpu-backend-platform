package com.example.chalpuplatform.subscription.dto;

import com.example.chalpuplatform.subscription.domain.StoreSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {

    private Long subscriptionId;

    private Long storeId;

    private Boolean isActive;

    private Boolean notificationEnabled;

    private String message;

    public static SubscriptionResponse from(StoreSubscription subscription, String message) {
        return SubscriptionResponse.builder()
                .subscriptionId(subscription.getId())
                .storeId(subscription.getStoreId())
                .isActive(subscription.getIsActive())
                .notificationEnabled(subscription.getNotificationEnabled())
                .message(message)
                .build();
    }
}
