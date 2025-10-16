package com.example.chalpuplatform.fcm.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "FCM 디바이스 타입", example = "ANDROID")
public enum DeviceType {

    @Schema(description = "안드로이드 기기")
    ANDROID("android"),

    @Schema(description = "iOS 기기")
    IOS("ios");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    public static DeviceType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("디바이스 타입이 비어있습니다.");
        }

        for (DeviceType type : values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("지원하지 않는 디바이스 타입입니다: " + value);
    }
}
