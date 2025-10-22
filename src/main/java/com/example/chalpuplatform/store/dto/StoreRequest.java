package com.example.chalpuplatform.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 생성/수정 요청")
public class StoreRequest {
    
    @Schema(description = "매장명", example = "맛있는 식당", required = true)
    private String storeName;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "배달의민족 링크", example = "https://baemin.com/store/12345")
    private String baeminLink;

    @Schema(description = "요기요 링크", example = "https://yogiyo.com/store/12345")
    private String yogiyoLink;

    @Schema(description = "쿠팡이츠 링크", example = "https://coupangeats.com/store/12345")
    private String coupangeatsLink;

    @Schema(description = "네이버 지도 링크", example = "https://map.naver.com/v5/entry/place/12345")
    private String naverLink;

    @Schema(description = "카카오맵 링크", example = "https://place.map.kakao.com/12345")
    private String kakaoLink;

    @Schema(description = "인스타그램 링크", example = "https://instagram.com/store_account")
    private String instagramLink;

    @Schema(description = "카카오톡 채널 링크", example = "https://pf.kakao.com/_store")
    private String kakaoTalkLink;

    @Schema(description = "사이트 링크 (URL 경로용)", example = "우리집냉면")
    private String siteLink;

    @Schema(description ="구글맵 링크", example = "https://maps.google.com/?q=37.5665,126.9780")
    private String googleMapsLink;

    @Schema(description="땡겨요 링크", example = "https://ddangyo.com/store/12345")
    private String ddangyoLink;

    @Schema(description = "당근 링크", example = "https://daangn.com/store/12345")
    private String daangnLink;

    @Schema(description = "가게 설명", example = "저희 가게는 신선한 재료로 음식을 만듭니다.")
    private String description;

    @Schema(description = "쿠폰 사용 개수", example = "10", required = false)
    private Integer requiredStampsForCoupon;

    @Schema(description = "템플릿 유형", example = "1", required = false)
    private Integer displayTemplate;
} 