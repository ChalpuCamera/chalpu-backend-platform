package com.example.chalpuplatform.reward.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리워드 교환 요청")
public class RewardRedemptionRequest {

    @Schema(description = "리워드 ID", example = "1")
    private Long rewardId;
}