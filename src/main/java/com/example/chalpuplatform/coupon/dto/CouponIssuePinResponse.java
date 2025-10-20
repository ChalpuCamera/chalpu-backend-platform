package com.example.chalpuplatform.coupon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "쿠폰 PIN 발급 응답")
public class CouponIssuePinResponse {

    @Schema(description = "발급된 PIN", example = "47")
    private String pin;

    @Schema(description = "스탬프 개수", example = "2")
    private Integer stamps;

    @Schema(description = "만료 시간")
    private LocalDateTime expiredAt;
}
