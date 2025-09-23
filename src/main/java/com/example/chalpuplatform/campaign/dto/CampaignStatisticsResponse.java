package com.example.chalpuplatform.campaign.dto;

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
public class CampaignStatisticsResponse {

    private Long campaignId;
    private String campaignName;
    private Integer targetFeedbackCount;
    private Long totalFeedbackCount;
    private Double progressRate;
    private Double averageSatisfaction;
    private Integer daysRemaining;
    private Integer totalDays;
    private List<DailyFeedbackCount> dailyFeedbacks;
    private Map<String, Long> feedbackByRating;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyFeedbackCount {
        private String date;
        private Long count;
        private Long cumulativeCount;
    }
}