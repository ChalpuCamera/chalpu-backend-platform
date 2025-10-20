package com.example.chalpuplatform.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쿠폰 PIN 발급 요청")
public class CouponIssuePinRequest {

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "지급할 스탬프 개수", example = "2")
    private Integer stamps;
}
