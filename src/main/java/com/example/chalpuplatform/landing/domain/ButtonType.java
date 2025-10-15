package com.example.chalpuplatform.landing.domain;

import lombok.Getter;

@Getter
public enum ButtonType {
    START_FREE("무료로 시작하기"),
    KAKAO_LOGIN("카카오 로그인"),
    LOGIN("로그인");

    private final String description;

    ButtonType(String description) {
        this.description = description;
    }
}
