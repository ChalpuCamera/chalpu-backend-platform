package com.example.chalpuplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private String title;

    private String body;

    @Builder.Default
    private Map<String, String> data = Map.of();
}
