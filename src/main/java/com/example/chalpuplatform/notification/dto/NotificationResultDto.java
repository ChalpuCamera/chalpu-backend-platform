package com.example.chalpuplatform.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResultDto {

    private Integer successCount;

    private Integer failureCount;

    private String message;

    private List<Long> successfulUserIds;

    public static NotificationResultDto multipleResult(
            int successCount,
            int failureCount,
            String message,
            List<Long> successfulUserIds) {

        return NotificationResultDto.builder()
                .successCount(successCount)
                .failureCount(failureCount)
                .message(message)
                .successfulUserIds(successfulUserIds)
                .build();
    }
}
