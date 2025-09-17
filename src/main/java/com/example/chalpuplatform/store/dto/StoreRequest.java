package com.example.chalpuplatform.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "가게 설명", example = "저희 가게는 신선한 재료로 음식을 만듭니다.")
    private String description;
} 