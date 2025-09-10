package com.example.chalpuplatform.reward.dto;

import com.example.chalpuplatform.reward.domain.Reward;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "리워드 정보")
public class RewardResponse {

    @Schema(description = "리워드 ID", example = "1")
    private Long id;

    @Schema(description = "리워드 이름", example = "올리브영 1만원 상품권")
    private String rewardName;

    @Schema(description = "리워드 타입", example = "coupon")
    private String rewardType;

    @Schema(description = "리워드 가치", example = "10000")
    private Integer rewardValue;

    @Schema(description = "필요한 리워드 카운트", example = "5")
    private Integer requiredCount;

    @Schema(description = "리워드 설명", example = "올리브영에서 사용 가능한 1만원 상품권")
    private String description;

    @Schema(description = "유효 기간")
    private LocalDate expiryDate;

    @Schema(description = "사용 가능 여부", example = "true")
    private Boolean available;

    public static RewardResponse from(Reward reward) {
        return RewardResponse.builder()
                .id(reward.getId())
                .rewardName(reward.getRewardName())
                .rewardType(reward.getRewardType())
                .rewardValue(reward.getRewardValue())
                .requiredCount(reward.getRequiredCount())
                .description(reward.getDescription())
                .expiryDate(reward.getExpiryDate())
                .available(reward.isAvailable())
                .build();
    }
}