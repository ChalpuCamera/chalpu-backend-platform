package com.example.chalpuplatform.reward.dto;

import com.example.chalpuplatform.reward.domain.RewardRedemption;
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
@Schema(description = "리워드 교환 응답")
public class RewardRedemptionResponse {

    @Schema(description = "교환 ID", example = "1")
    private Long id;

    @Schema(description = "리워드 이름", example = "올리브영 1만원 상품권")
    private String rewardName;

    @Schema(description = "교환 시 리워드 카운트", example = "10")
    private Integer rewardCount;

    @Schema(description = "교환 상태", example = "ISSUED")
    private RewardRedemption.RedemptionStatus status;

    @Schema(description = "교환일시")
    private LocalDateTime redeemedAt;

    public static RewardRedemptionResponse from(RewardRedemption redemption) {
        return RewardRedemptionResponse.builder()
                .id(redemption.getId())
                .rewardName(redemption.getReward().getRewardName())
                .rewardCount(redemption.getRewardCount())
                .status(redemption.getStatus())
                .redeemedAt(redemption.getCreatedAt())
                .build();
    }
}