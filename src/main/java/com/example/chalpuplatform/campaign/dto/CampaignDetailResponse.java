package com.example.chalpuplatform.campaign.dto;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignDetailResponse {

    private Long id;
    private String name;
    private String description;
    private Long storeId;
    private String storeName;
    private Long foodItemId;
    private String foodItemName;
    private Integer targetFeedbackCount;
    private Long currentFeedbackCount;
    private Double progressRate;
    private String status;
    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static CampaignDetailResponse from(Campaign campaign, long currentFeedbackCount) {
        return CampaignDetailResponse.builder()
            .id(campaign.getId())
            .name(campaign.getName())
            .description(campaign.getDescription())
            .storeId(campaign.getStore().getId())
            .storeName(campaign.getStore().getStoreName())
            .foodItemId(campaign.getFoodItem().getId())
            .foodItemName(campaign.getFoodItem().getFoodName())
            .targetFeedbackCount(campaign.getTargetFeedbackCount())
            .currentFeedbackCount(currentFeedbackCount)
            .progressRate(campaign.calculateProgressRate(currentFeedbackCount))
            .status(campaign.getStatus().getKorean())
            .isActive(campaign.isActive())
            .startDate(campaign.getStartDate())
            .endDate(campaign.getEndDate())
            .createdAt(campaign.getCreatedAt())
            .updatedAt(campaign.getUpdatedAt())
            .build();
    }
}