package com.example.chalpuplatform.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Schema(description = "가게 설명", example = "저희 가게는 신선한 재료로 음식을 만듭니다.")
    private String description;

    @Schema(description = "쿠폰 사용 개수", example = "10", required = false)
    private Integer requiredStampsForCoupon;

    @Schema(description = "템플릿 유형", example = "1", required = false)
    private Integer displayTemplate;

    @Schema(description = "사장님의 메뉴가 자동으로 자신의 가게에 등록되는 기능 on/off")
    private Boolean autoCreateMenus;

    @Schema(description = "매장 고유 링크 (사이트 식별자)", example = "우리집냉면", required = true)
    private String siteLink;

    @Schema(description = "매장 링크 목록 (배민, 요기요, 인스타 등)")
    private List<StoreLinkRequest> links;
} 