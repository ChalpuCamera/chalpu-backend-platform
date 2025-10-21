package com.example.chalpuplatform.notification.dto;

import com.example.chalpuplatform.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    private Long storeId;

    private NotificationType type;

    private String title;

    private String message;

    private LocalDateTime scheduledAt;

    private Map<String, String> data;
}
