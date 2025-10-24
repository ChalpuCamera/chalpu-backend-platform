package com.example.chalpuplatform.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쿠폰 사용 요청")
public class CouponEarnIsSuccessDTO {

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "핀 번호", example = "54")
    private String pin;
}