package com.example.chalpuplatform.campaign.service;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.dto.CampaignDetailResponse;
import com.example.chalpuplatform.campaign.dto.CampaignResponse;
import com.example.chalpuplatform.campaign.dto.CampaignStatisticsResponse;
import com.example.chalpuplatform.campaign.dto.GetCampaignsByStoreRequest;
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
@Transactional
@Slf4j
public class CampaignQueryService {

    private final CampaignRepository campaignRepository;
    private final CustomerFeedbackRepository customerFeedbackRepository;
    private final StoreRepository storeRepository;
    private final CampaignDomainService campaignDomainService;

    public CampaignDetailResponse getCampaignById(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        // currentFeedbackCount 필드 직접 사용
        Integer currentFeedbackCount = campaign.getCurrentFeedbackCount();

        // 목표 달성 시 자동 완료 처리 (조회 시에도 체크)
        if (campaignDomainService.shouldAutoComplete(campaign, currentFeedbackCount)) {
            updateCampaignStatus(campaign);
        }

        return CampaignDetailResponse.from(campaign, currentFeedbackCount);
    }

    protected void updateCampaignStatus(Campaign campaign) {
        campaign.complete();
        campaignRepository.save(campaign);
    }

    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> getCampaignsByStore(GetCampaignsByStoreRequest request, Pageable pageable) {
        Store store = storeRepository.findById(request.getStoreId())
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_NOT_FOUND));

        Page<Campaign> campaigns;

        if (request.getStatus() != null) {
            // 상태가 지정된 경우 필터링
            campaigns = campaignRepository.findByStoreAndStatusAndIsActiveTrue(store, request.getStatus(), pageable);
        } else {
            // 상태가 지정되지 않은 경우 모든 활성 캠페인
            campaigns = campaignRepository.findByStoreAndIsActiveTrue(store, pageable);
        }

        Page<CampaignResponse> campaignResponses = campaigns.map(CampaignResponse::from);

        return PageResponse.from(campaignResponses);
    }

    @Transactional(readOnly = true)
    public CampaignDetailResponse getCampaignWithProgress(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        // currentFeedbackCount 필드 직접 사용
        Integer currentFeedbackCount = campaign.getCurrentFeedbackCount();

        return CampaignDetailResponse.from(campaign, currentFeedbackCount);
    }

    @Transactional(readOnly = true)
    public CampaignStatisticsResponse getCampaignStatistics(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        // currentFeedbackCount 필드 직접 사용
        long totalFeedbackCount = campaign.getCurrentFeedbackCount();
        Double averageSatisfaction = getAverageSatisfaction(campaign);
        List<CampaignStatisticsResponse.DailyFeedbackCount> dailyFeedbacks = getDailyFeedbackCounts(campaign);

        // 남은 일수 계산
        int daysRemaining = calculateDaysRemaining(campaign.getEndDate());
        int totalDays = calculateTotalDays(campaign.getStartDate(), campaign.getEndDate());

        return CampaignStatisticsResponse.builder()
            .campaignId(campaign.getId())
            .campaignName(campaign.getName())
            .targetFeedbackCount(campaign.getTargetFeedbackCount())
            .totalFeedbackCount(totalFeedbackCount)
            .averageSatisfaction(averageSatisfaction)
            .daysRemaining(daysRemaining)
            .totalDays(totalDays)
            .dailyFeedbacks(dailyFeedbacks)
            .build();
    }

    private Double getAverageSatisfaction(Campaign campaign) {
        // 캠페인 ID로 직접 조회하도록 변경
        return customerFeedbackRepository.findAverageSatisfactionByCampaignId(campaign.getId());
    }

    private List<CampaignStatisticsResponse.DailyFeedbackCount> getDailyFeedbackCounts(Campaign campaign) {
        // 캠페인 ID로 직접 조회하도록 변경
        List<Object[]> dailyCounts = customerFeedbackRepository.findDailyFeedbackCountsByCampaignId(campaign.getId());

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

        return dailyFeedbacks;
    }

    private int calculateDaysRemaining(LocalDateTime endDate) {
        if (endDate.isAfter(LocalDateTime.now())) {
            return (int) ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
        }
        return 0;
    }

    private int calculateTotalDays(LocalDateTime startDate, LocalDateTime endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }


}