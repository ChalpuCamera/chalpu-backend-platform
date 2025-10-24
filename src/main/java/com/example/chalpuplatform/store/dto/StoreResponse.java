package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 응답")
public class StoreResponse {
    
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;
    
    @Schema(description = "매장명", example = "맛있는 식당")
    private String storeName;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "배달의 민족 링크", example = "https://baemin.me/restaurant/12345")
    private String baeminLink;

    @Schema(description = "요기요 링크", example = "https://www.yogiyo.co.kr/restaurant/12345")
    private String yogiyoLink;

    @Schema(description = "쿠팡이츠 링크", example = "https://www.coupang.com/restaurant/12345")
    private String coupangEatsLink;

    @Schema(description = "네이버 지도 링크", example = "https://map.naver.com/v5/entry/place/12345")
    private String naverLink;

    @Schema(description = "카카오맵 링크", example = "https://place.map.kakao.com/12345")
    private String kakaoLink;

    @Schema(description = "인스타그램 링크", example = "https://instagram.com/store_account")
    private String instagramLink;

    @Schema(description = "카카오톡 채널 링크", example = "https://pf.kakao.com/_store")
    private String kakaoTalkLink;

    @Schema(description ="구글맵 링크", example = "https://maps.google.com/?q=37.5665,126.9780")
    private String googleMapsLink;

    @Schema(description="땡겨요 링크", example = "https://ddangyo.com/store/12345")
    private String ddangyoLink;

    @Schema(description = "당근 링크", example = "https://daangn.com/store/12345")
    private String daangnLink;

    @Schema(description = "사이트 링크 (URL 경로용)", example = "우리집냉면")
    private String siteLink;

    @Schema(description = "가게 설명", example = "저희 가게는 신선한 재료로 음식을 만듭니다.")
    private String description;

    @Schema(description = "피드백 개수", example = "42")
    private Long feedbackCount;

    @Schema(description = "메뉴 개수", example = "15")
    private Long menuCount;

    @Schema(description = "썸네일 URL", example = "https://chalpu.s3.ap-northeast-2.amazonaws.com/stores/1/thumbnail.jpg")
    private String thumbnailUrl;

    @Schema(description = "쿠폰 사용 개수", example = "10", required = false)
    private Integer requiredStampsForCoupon;

    @Schema(description = "템플릿 유형", example = "1", required = false)
    private Integer displayTemplate;

    @Schema(description = "사장님의 메뉴가 자동으로 자신의 가게에 등록되는 기능 on/off")
    private Boolean autoCreateMenus;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .description(store.getDescription())
                .feedbackCount(store.getFeedbackCount())
                .menuCount(store.getMenuCount())
                .thumbnailUrl(store.getThumbnailUrl())
                .baeminLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getBaeminLink() != null
                        ? store.getDeliveryPlatformLinks().getBaeminLink() : "")
                .yogiyoLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getYogiyoLink() != null
                        ? store.getDeliveryPlatformLinks().getYogiyoLink() : "")
                .coupangEatsLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getCoupangeatsLink() != null
                        ? store.getDeliveryPlatformLinks().getCoupangeatsLink() : "")
                .naverLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getNaverLink() != null
                        ? store.getDeliveryPlatformLinks().getNaverLink() : "")
                .kakaoLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getKakaoLink() != null
                        ? store.getDeliveryPlatformLinks().getKakaoLink() : "")
                .instagramLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getInstagramLink() != null
                        ? store.getDeliveryPlatformLinks().getInstagramLink() : "")
                .kakaoTalkLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getKakaoTalkLink() != null
                        ? store.getDeliveryPlatformLinks().getKakaoTalkLink() : "")
                .siteLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getSiteLink() != null
                        ? store.getDeliveryPlatformLinks().getSiteLink() : "")
                .googleMapsLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getGoogleMapsLink() != null
                        ? store.getDeliveryPlatformLinks().getGoogleMapsLink() : "")
                .ddangyoLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getDdangyoLink() != null
                        ? store.getDeliveryPlatformLinks().getDdangyoLink() : "")
                .daangnLink(store.getDeliveryPlatformLinks() != null && store.getDeliveryPlatformLinks().getDaangnLink() != null
                        ? store.getDeliveryPlatformLinks().getDaangnLink() : "")
                .requiredStampsForCoupon(store.getRequiredStampsForCoupon())
                .displayTemplate(store.getDisplayTemplate() != null ? store.getDisplayTemplate() : 1)
                .autoCreateMenus(store.getAutoCreateMenus() != null ? store.getAutoCreateMenus() : false)
                .build();
    }
} 