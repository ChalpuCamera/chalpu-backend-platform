package com.example.chalpuplatform.coupon.dto;

import com.example.chalpuplatform.coupon.domain.CouponPinHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinStatusResponse {

    private Boolean isUsed;

    private Boolean isExpired;

    private LocalDateTime expiredAt;

    private Integer stamps;

    public static PinStatusResponse from(CouponPinHistory pinHistory) {
        return PinStatusResponse.builder()
                .isUsed(pinHistory.getIsUsed())
                .isExpired(pinHistory.isExpired())
                .expiredAt(pinHistory.getExpiredAt())
                .stamps(pinHistory.getStamps())
                .build();
    }
}
