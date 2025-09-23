package com.example.chalpuplatform.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "캠페인 통계 응답")
public class CampaignStatisticsResponse {

    @Schema(description = "캠페인 ID", example = "1")
    private Long campaignId;

    @Schema(description = "캠페인 이름", example = "신메뉴 런칭 캠페인")
    private String campaignName;

    @Schema(description = "목표 피드백 수", example = "100")
    private Integer targetFeedbackCount;

    @Schema(description = "총 피드백 수", example = "45")
    private Long totalFeedbackCount;

    @Schema(description = "진행률 (%)", example = "45.0")
    private Double progressRate;

    @Schema(description = "평균 만족도", example = "4.5")
    private Double averageSatisfaction;

    @Schema(description = "남은 일수", example = "15")
    private Integer daysRemaining;

    @Schema(description = "총 캠페인 일수", example = "30")
    private Integer totalDays;

    @Schema(description = "일별 피드백 현황")
    private List<DailyFeedbackCount> dailyFeedbacks;

    @Schema(description = "평점별 피드백 수")
    private Map<String, Long> feedbackByRating;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "일별 피드백 수")
    public static class DailyFeedbackCount {
        @Schema(description = "날짜", example = "2024-01-01")
        private String date;

        @Schema(description = "해당일 피드백 수", example = "5")
        private Long count;

        @Schema(description = "누적 피드백 수", example = "45")
        private Long cumulativeCount;
    }
}