package com.example.chalpuplatform.campaign.dto;

import com.example.chalpuplatform.campaign.domain.Campaign;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "캠페인 상태 변경 요청")
public class ChangeCampaignStatusRequest {

    @NotNull
    @Schema(description = "캠페인 ID", example = "1", required = true)
    private Long campaignId;

    @NotNull
    @Schema(description = "변경할 캠페인 상태 (DRAFT, ACTIVE, PAUSED, COMPLETED, EXPIRED)", example = "ACTIVE", required = true)
    private Campaign.CampaignStatus status;
}