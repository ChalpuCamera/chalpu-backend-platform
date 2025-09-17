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

    @Schema(description = "가게 설명", example = "저희 가게는 신선한 재료로 음식을 만듭니다.")
    private String description;
    

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .description(store.getDescription())
                .baeminLink(store.getDeliveryPlatformLinks().getBaeminLink() != null ? store.getDeliveryPlatformLinks().getBaeminLink() : "")
                .yogiyoLink(store.getDeliveryPlatformLinks().getYogiyoLink() != null ? store.getDeliveryPlatformLinks().getYogiyoLink() : "")
                .coupangEatsLink(store.getDeliveryPlatformLinks().getCoupangeatsLink() != null ? store.getDeliveryPlatformLinks().getCoupangeatsLink() : "")
                .build();
    }
} 