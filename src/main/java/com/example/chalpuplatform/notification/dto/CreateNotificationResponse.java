package com.example.chalpuplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationResponse {

    private Long notificationId;

    private Integer targetSubscriberCount;

    private String message;

    public static CreateNotificationResponse of(Long notificationId, Integer targetSubscriberCount) {
        return CreateNotificationResponse.builder()
                .notificationId(notificationId)
                .targetSubscriberCount(targetSubscriberCount)
                .message(String.format("알림 생성 완료: %d명에게 발송 예정", targetSubscriberCount))
                .build();
    }
}
