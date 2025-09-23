package com.example.chalpuplatform.campaign.dto;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "캠페인 기본 응답")
public class CampaignResponse {

    @Schema(description = "캠페인 ID", example = "1")
    private Long id;

    @Schema(description = "캠페인 이름", example = "신메뉴 런칭 캠페인")
    private String name;

    @Schema(description = "캠페인 설명", example = "신메뉴 출시를 기념하여 고객 피드백을 수집합니다")
    private String description;

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장명", example = "맛있는 식당")
    private String storeName;

    @Schema(description = "음식 ID", example = "5")
    private Long foodItemId;

    @Schema(description = "음식명", example = "김치찌개")
    private String foodItemName;

    @Schema(description = "목표 피드백 수", example = "100")
    private Integer targetFeedbackCount;

    @Schema(description = "캠페인 상태", example = "활성")
    private String status;

    @Schema(description = "캠페인 시작일", example = "2024-01-01 00:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @Schema(description = "캠페인 종료일", example = "2024-12-31 23:59:59")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @Schema(description = "생성일시", example = "2024-01-01 00:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-01 00:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static CampaignResponse from(Campaign campaign) {
        return CampaignResponse.builder()
            .id(campaign.getId())
            .name(campaign.getName())
            .description(campaign.getDescription())
            .storeId(campaign.getStore().getId())
            .storeName(campaign.getStore().getStoreName())
            .foodItemId(campaign.getFoodItem().getId())
            .foodItemName(campaign.getFoodItem().getFoodName())
            .targetFeedbackCount(campaign.getTargetFeedbackCount())
            .status(campaign.getStatus().getKorean())
            .startDate(campaign.getStartDate())
            .endDate(campaign.getEndDate())
            .createdAt(campaign.getCreatedAt())
            .updatedAt(campaign.getUpdatedAt())
            .build();
    }
}