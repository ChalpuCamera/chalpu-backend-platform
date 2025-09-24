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
@Schema(description = "매장별 캠페인 조회 요청")
public class GetCampaignsByStoreRequest {

    @NotNull
    @Schema(description = "매장 ID", example = "1", required = true)
    private Long storeId;

    @Schema(description = "캠페인 상태 필터 (선택, null이면 모든 상태)", example = "ACTIVE")
    private Campaign.CampaignStatus status;
}