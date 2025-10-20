package com.example.chalpuplatform.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "쿠폰 스탬프 적립 응답")
public class CouponEarnResponse {

    @Schema(description = "적립 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "현재 스탬프 개수", example = "7")
    private Integer currentStamps;
}
