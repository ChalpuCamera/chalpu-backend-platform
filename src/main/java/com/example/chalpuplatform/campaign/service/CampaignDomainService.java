package com.example.chalpuplatform.campaign.service;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.repository.CampaignRepository;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignDomainService {

    private static final int MIN_TARGET_FEEDBACK_COUNT = 1;
    private static final int MAX_TARGET_FEEDBACK_COUNT = 100;
    private static final long DEFAULT_CAMPAIGN_ID = 0L;

    private final CampaignRepository campaignRepository;

    public void validateCampaignCreation(FoodItem foodItem, LocalDateTime startDate, LocalDateTime endDate) {
        // 날짜 유효성 검증
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다");
        }

        if (endDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("종료일은 현재 시간 이후여야 합니다");
        }

        // 중복 캠페인 검증
        boolean hasOverlapping = campaignRepository.existsOverlappingCampaign(
            foodItem, startDate, endDate, DEFAULT_CAMPAIGN_ID
        );

        if (hasOverlapping) {
            throw new IllegalStateException("해당 기간에 이미 진행 중이거나 예정된 캠페인이 있습니다");
        }
    }

    public void validateCampaignUpdate(Campaign campaign, LocalDateTime startDate, LocalDateTime endDate) {
        if (!campaign.canBeModified()) {
            throw new IllegalStateException("활성 또는 완료된 캠페인은 수정할 수 없습니다");
        }

        // 날짜 유효성 검증
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다");
        }

        // 중복 캠페인 검증 (자기 자신 제외)
        boolean hasOverlapping = campaignRepository.existsOverlappingCampaign(
            campaign.getFoodItem(), startDate, endDate, campaign.getId()
        );

        if (hasOverlapping) {
            throw new IllegalStateException("해당 기간에 이미 진행 중이거나 예정된 캠페인이 있습니다");
        }
    }

    public boolean shouldAutoComplete(Campaign campaign, long currentFeedbackCount) {
        return campaign.getStatus() == Campaign.CampaignStatus.ACTIVE
            && campaign.isTargetReached(currentFeedbackCount);
    }

    public void processCampaignCompletion(Campaign campaign, long currentFeedbackCount) {
        if (shouldAutoComplete(campaign, currentFeedbackCount)) {
            campaign.complete();
            log.info("캠페인 자동 완료 처리: campaignId={}, currentFeedbackCount={}, target={}",
                campaign.getId(), currentFeedbackCount, campaign.getTargetFeedbackCount());
        }
    }

    public void processCampaignExpiration(Campaign campaign) {
        if (campaign.isExpired() && campaign.getStatus() != Campaign.CampaignStatus.COMPLETED) {
            campaign.expire();
            log.info("캠페인 만료 처리: campaignId={}", campaign.getId());
        }
    }

    public void validateTargetFeedbackCount(Integer targetCount) {
        if (targetCount == null || targetCount < MIN_TARGET_FEEDBACK_COUNT) {
            throw new IllegalArgumentException("목표 피드백 수는 " + MIN_TARGET_FEEDBACK_COUNT + "개 이상이어야 합니다");
        }

        if (targetCount > MAX_TARGET_FEEDBACK_COUNT) {
            throw new IllegalArgumentException("목표 피드백 수는 " + MAX_TARGET_FEEDBACK_COUNT + "개를 초과할 수 없습니다");
        }
    }
}