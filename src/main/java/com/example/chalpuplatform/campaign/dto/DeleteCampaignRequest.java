package com.example.chalpuplatform.campaign.dto;

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
@Schema(description = "캠페인 삭제 요청")
public class DeleteCampaignRequest {

    @NotNull
    @Schema(description = "삭제할 캠페인 ID", example = "1", required = true)
    private Long campaignId;
}