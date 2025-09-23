package com.example.chalpuplatform.campaign.service;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.dto.CampaignDetailResponse;
import com.example.chalpuplatform.campaign.dto.CampaignResponse;
import com.example.chalpuplatform.campaign.dto.CampaignStatisticsResponse;
import com.example.chalpuplatform.campaign.repository.CampaignRepository;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.common.exception.CampaignException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.customerfeedback.repository.CustomerFeedbackRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CampaignQueryService {

    private final CampaignRepository campaignRepository;
    private final CustomerFeedbackRepository customerFeedbackRepository;
    private final StoreRepository storeRepository;
    private final CampaignDomainService campaignDomainService;

    public CampaignDetailResponse getCampaignById(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        long currentFeedbackCount = getCurrentFeedbackCount(campaign);

        // 목표 달성 시 자동 완료 처리 (조회 시에도 체크)
        if (campaignDomainService.shouldAutoComplete(campaign, currentFeedbackCount)) {
            campaign.complete();
            campaignRepository.save(campaign);
        }

        return CampaignDetailResponse.from(campaign, currentFeedbackCount);
    }

    public PageResponse<CampaignResponse> getCampaignsByStore(Long storeId, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_NOT_FOUND));

        Page<Campaign> campaigns = campaignRepository.findByStoreAndIsActiveTrue(store, pageable);

        Page<CampaignResponse> campaignResponses = campaigns.map(CampaignResponse::from);

        return PageResponse.from(campaignResponses);
    }

    public List<CampaignResponse> getActiveCampaignsByStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_NOT_FOUND));

        List<Campaign> activeCampaigns = campaignRepository.findActiveCampaignsByStore(
            store, LocalDateTime.now()
        );

        return activeCampaigns.stream()
            .map(CampaignResponse::from)
            .collect(Collectors.toList());
    }

    public CampaignDetailResponse getCampaignWithProgress(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        long currentFeedbackCount = getCurrentFeedbackCount(campaign);

        return CampaignDetailResponse.from(campaign, currentFeedbackCount);
    }

    public CampaignStatisticsResponse getCampaignStatistics(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        // 현재 피드백 수
        long totalFeedbackCount = getCurrentFeedbackCount(campaign);

        // 평균 만족도
        Double averageSatisfaction = customerFeedbackRepository.findAverageSatisfactionForCampaign(
            campaign.getFoodItem(),
            campaign.getStore(),
            campaign.getStartDate(),
            campaign.getEndDate()
        );

        // 일별 피드백 수
        List<Object[]> dailyCounts = customerFeedbackRepository.findDailyFeedbackCounts(
            campaign.getFoodItem(),
            campaign.getStore(),
            campaign.getStartDate(),
            campaign.getEndDate()
        );

        List<CampaignStatisticsResponse.DailyFeedbackCount> dailyFeedbacks = new ArrayList<>();
        long cumulativeCount = 0;

        for (Object[] row : dailyCounts) {
            LocalDate date = (LocalDate) row[0];
            Long count = (Long) row[1];
            cumulativeCount += count;

            dailyFeedbacks.add(
                CampaignStatisticsResponse.DailyFeedbackCount.builder()
                    .date(date.toString())
                    .count(count)
                    .cumulativeCount(cumulativeCount)
                    .build()
            );
        }

        // 남은 일수 계산
        int daysRemaining = 0;
        if (campaign.getEndDate().isAfter(LocalDateTime.now())) {
            daysRemaining = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), campaign.getEndDate());
        }

        int totalDays = (int) ChronoUnit.DAYS.between(campaign.getStartDate(), campaign.getEndDate());

        return CampaignStatisticsResponse.builder()
            .campaignId(campaign.getId())
            .campaignName(campaign.getName())
            .targetFeedbackCount(campaign.getTargetFeedbackCount())
            .totalFeedbackCount(totalFeedbackCount)
            .progressRate(campaign.calculateProgressRate(totalFeedbackCount))
            .averageSatisfaction(averageSatisfaction)
            .daysRemaining(daysRemaining)
            .totalDays(totalDays)
            .dailyFeedbacks(dailyFeedbacks)
            .build();
    }

    public Map<String, Object> getDashboardData(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_NOT_FOUND));

        // 활성 캠페인 목록
        List<Campaign> activeCampaigns = campaignRepository.findActiveCampaignsByStore(
            store, LocalDateTime.now()
        );

        // 각 캠페인의 진행 상황
        List<Map<String, Object>> campaignProgress = new ArrayList<>();
        for (Campaign campaign : activeCampaigns) {
            long currentCount = getCurrentFeedbackCount(campaign);
            Map<String, Object> progress = new HashMap<>();
            progress.put("campaignId", campaign.getId());
            progress.put("campaignName", campaign.getName());
            progress.put("foodItemName", campaign.getFoodItem().getFoodName());
            progress.put("targetCount", campaign.getTargetFeedbackCount());
            progress.put("currentCount", currentCount);
            progress.put("progressRate", campaign.calculateProgressRate(currentCount));
            progress.put("status", campaign.getStatus().getKorean());
            progress.put("daysRemaining", ChronoUnit.DAYS.between(LocalDateTime.now(), campaign.getEndDate()));
            campaignProgress.add(progress);
        }

        // 전체 통계
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("activeCampaignCount", activeCampaigns.size());
        dashboard.put("campaigns", campaignProgress);
        dashboard.put("lastUpdated", LocalDateTime.now());

        return dashboard;
    }

    private long getCurrentFeedbackCount(Campaign campaign) {
        return customerFeedbackRepository.countByFoodItemAndStoreBetweenDates(
            campaign.getFoodItem(),
            campaign.getStore(),
            campaign.getStartDate(),
            campaign.getEndDate()
        );
    }
}