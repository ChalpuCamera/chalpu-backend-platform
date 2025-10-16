package com.example.chalpuplatform.oauth.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 제공자 유형")
public enum AuthProvider {
    @Schema(description = "Kakao 로그인") KAKAO,
    @Schema(description = "Google 로그인") GOOGLE,
    @Schema(description = "Apple 로그인") APPLE,
    @Schema(description = "Naver 로그인") NAVER;
    /**
     * 문자열로부터 AuthProvider를 반환합니다.
     * @param value 인증 제공자 문자열 (대소문자 무관)
     * @return 해당하는 AuthProvider
     * @throws IllegalArgumentException 지원하지 않는 인증 제공자인 경우
     */
    public static AuthProvider from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("인증 제공자가 비어있습니다.");
        }

        for (AuthProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(value.trim())) {
                return provider;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 인증 제공자입니다: " + value);
    }

    /**
     * 인증 제공자가 유효한지 확인합니다.
     * @param value 인증 제공자 문자열
     * @return 유효 여부
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        for (AuthProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }
}
