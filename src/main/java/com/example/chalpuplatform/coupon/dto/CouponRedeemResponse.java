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
@Schema(description = "쿠폰 사용 응답")
public class CouponRedeemResponse {

    @Schema(description = "사용 성공 여부", example = "true")
    private Boolean success;

    @Schema(description = "남은 스탬프 개수", example = "0")
    private Integer currentStamps;
}
