package com.example.chalpuplatform.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사장님용 스탬프 적립 요청")
public class CouponEarnStampsByOwnerRequest {

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "고객이 생성한 PIN", example = "47")
    private String pin;

    @Schema(description = "적립할 스탬프 개수", example = "2")
    private Integer stamps;
}
