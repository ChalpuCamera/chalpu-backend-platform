package com.example.chalpuplatform.oauth.dto.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Apple 로그인 요청")
public class AppleLoginRequest implements LoginRequest {
    
    @Schema(description = "Apple Identity Token (JWT)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
    private String accessToken;
    
    @Schema(description = "FCM 토큰", example = "fcm_token_here")
    private String fcmToken;
    
    @Schema(description = "디바이스 타입", example = "ios")
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
