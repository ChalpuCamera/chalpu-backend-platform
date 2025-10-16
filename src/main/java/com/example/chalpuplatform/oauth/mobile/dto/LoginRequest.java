package com.example.chalpuplatform.oauth.mobile.dto;

import com.example.chalpuplatform.fcm.domain.DeviceType;

/**
 * OAuth 로그인 요청 공통 인터페이스
 * 다형성을 활용하여 각 OAuth provider별 요청을 추상화
 */
public interface LoginRequest {

    /**
     * 액세스 토큰 또는 ID 토큰 반환
     */
    String getAccessToken();

    /**
     * FCM 토큰 반환
     */
    String getFcmToken();

    /**
     * 디바이스 타입 반환
     */
    DeviceType getDeviceType();

    /**
     * 디바이스 모델명 반환
     */
    String getDeviceModel();

    /**
     * OS 버전 반환
     */
    String getOsVersion();

    /**
     * 앱 버전 반환
     */
    String getAppVersion();

    /**
     * 푸시 알림 허용 여부 반환
     */
    Boolean getIsAllowed();

    /**
     * 사용자 타입 반환 (customer 또는 owner)
     */
    String getUserType();
}