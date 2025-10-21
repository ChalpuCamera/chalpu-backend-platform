package com.example.chalpuplatform.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeliveryPlatformLinks {

    @Column(name = "baemin_link", length = 255, nullable = true)
    private String baeminLink;

    @Column(name = "yogiyo_link", length = 255, nullable = true)
    private String yogiyoLink;

    @Column(name = "coupangeats_link", length = 255, nullable = true)
    private String coupangeatsLink;

    @Column(name = "naver_map_link", length = 255, nullable = true)
    private String naverLink;

    @Column(name = "kakao_map_link", length = 255, nullable = true)
    private String kakaoLink;

    @Column(name = "instagram_link", length = 255, nullable = true)
    private String instagramLink;

    @Column(name = "kakao_talk_link", length = 255, nullable = true)
    private String kakaoTalkLink;

    @Column(name ="google_maps_link", length = 255, nullable = true)
    private String googleMapsLink;

    @Column(name="ddangyo_link", length = 255, nullable = true)
    private String ddangyoLink;

    @Column(name = "daangn_link", length = 255, nullable = true)
    private String daangnLink;

    @Column(name = "site_link", length = 255, nullable = true)
    private String siteLink;
}