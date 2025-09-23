package com.example.chalpuplatform.campaign.dto;

import com.example.chalpuplatform.campaign.domain.Campaign;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeCampaignStatusRequest {

    @NotNull
    private Campaign.CampaignStatus status;
}