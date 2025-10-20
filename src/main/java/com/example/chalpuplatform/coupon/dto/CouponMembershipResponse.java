package com.example.chalpuplatform.coupon.dto;

import com.example.chalpuplatform.coupon.domain.CouponMembership;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "쿠폰 멤버십 조회 응답")
public class CouponMembershipResponse {

    @Schema(description = "현재 스탬프 개수", example = "5")
    private Integer currentStamps;

    @Schema(description = "쿠폰 사용에 필요한 스탬프 개수", example = "10")
    private Integer requiredStamps;

    @Schema(description = "사용 가능 여부", example = "false")
    private Boolean canRedeem;

    public static CouponMembershipResponse from(CouponMembership membership, Integer requiredStamps) {
        return CouponMembershipResponse.builder()
                .currentStamps(membership.getCurrentStamps())
                .requiredStamps(requiredStamps)
                .canRedeem(membership.canRedeem(requiredStamps))
                .build();
    }

    public static CouponMembershipResponse empty(Integer requiredStamps) {
        return CouponMembershipResponse.builder()
                .currentStamps(0)
                .requiredStamps(requiredStamps)
                .canRedeem(false)
                .build();
    }
}
