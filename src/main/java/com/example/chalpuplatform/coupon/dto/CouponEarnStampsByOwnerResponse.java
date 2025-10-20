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
@Schema(description = "사장님용 스탬프 적립 응답")
public class CouponEarnStampsByOwnerResponse {

    @Schema(description = "적립 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "적립 후 현재 스탬프 개수", example = "7")
    private Integer currentStamps;

    @Schema(description = "이번에 적립된 스탬프 개수", example = "2")
    private Integer addedStamps;
}
