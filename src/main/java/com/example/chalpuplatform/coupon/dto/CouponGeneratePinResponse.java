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
@Schema(description = "고객용 PIN 생성 응답")
public class CouponGeneratePinResponse {

    @Schema(description = "생성된 PIN (2자리)", example = "47")
    private String pin;

    @Schema(description = "만료 시간", example = "2025-10-20T15:23:00")
    private LocalDateTime expiredAt;
}
