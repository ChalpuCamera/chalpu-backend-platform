package com.example.chalpuplatform.store.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {
    BAEMIN("배달의민족"),
    YOGIYO("요기요"),
    COUPANGEATS("쿠팡이츠"),
    NAVER_MAP("네이버 지도"),
    KAKAO_MAP("카카오맵"),
    INSTAGRAM("인스타그램"),
    KAKAO_TALK("카카오톡 채널"),
    GOOGLE_MAPS("구글맵"),
    DDANGYO("땡겨요"),
    DAANGN("당근"),
    CUSTOM("커스텀");

    private final String defaultLabel;
}
