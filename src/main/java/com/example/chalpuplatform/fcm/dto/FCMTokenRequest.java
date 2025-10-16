package com.example.chalpuplatform.fcm.dto;

import com.example.chalpuplatform.fcm.domain.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FCMTokenRequest {
    private Long userId;
    private String fcmToken;
    private DeviceType deviceType;
    private String deviceModel;
    private String osVersion;
    private String appVersion;
    private Boolean isAllowed;
}
