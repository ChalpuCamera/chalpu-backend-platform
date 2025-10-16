package com.example.chalpuplatform.oauth.dto.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Google 모바일 로그인 요청 Body")
public class GoogleLoginRequest implements LoginRequest {

    @Schema(description = "Google Sign-In SDK로부터 발급받은 ID Token 문자열",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzRhYmM1Njc4Z...",
            required = true)
    private String accessToken;

    @Schema(description = "사용자 타입",
            example = "customer",
            allowableValues = {"customer", "owner"},
            required = true)
    private String userType;

    @Schema(description = "FCM 토큰", example = "fcm_token_here")
    private String fcmToken;

    @Schema(description = "디바이스 타입", example = "android")
    private String deviceType;

    @Schema(description = "디바이스 모델명", example = "iPhone 14 Pro")
    private String deviceModel;

    @Schema(description = "OS 버전", example = "16.5.1")
    private String osVersion;

    @Schema(description = "앱 버전", example = "1.0.0")
    private String appVersion;

    @Schema(description = "푸시 알림 허용 여부", example = "true")
    private Boolean isAllowed;
} 