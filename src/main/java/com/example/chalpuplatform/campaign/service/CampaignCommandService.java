package com.example.chalpuplatform.campaign.service;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.campaign.dto.CreateCampaignRequest;
import com.example.chalpuplatform.campaign.dto.UpdateCampaignRequest;
import com.example.chalpuplatform.campaign.repository.CampaignRepository;
import com.example.chalpuplatform.common.exception.CampaignException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CampaignCommandService {

    private final CampaignRepository campaignRepository;
    private final CampaignDomainService campaignDomainService;
    private final StoreRepository storeRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;

    public Long createCampaign(CreateCampaignRequest request, Long userId) {
        log.info("캠페인 생성 요청: userId={}, storeId={}, foodItemId={}",
            userId, request.getStoreId(), request.getFoodItemId());

        // 사용자 확인
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.USER_NOT_FOUND));

        // 매장 확인
        Store store = storeRepository.findById(request.getStoreId())
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_NOT_FOUND));

        UserStoreRole usr = userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(userId, request.getStoreId())
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_ACCESS_DENIED));

        // 캠페인 관리 권한 확인
        if (!usr.canManageStore()) {
            throw new CampaignException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        // 음식 확인
        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
            .orElseThrow(() -> new CampaignException(ErrorMessage.FOODITEM_NOT_FOUND));

        // 음식이 해당 매장의 것인지 확인
        if (!foodItem.getStore().getId().equals(store.getId())) {
            throw new IllegalArgumentException("해당 음식은 선택한 매장의 메뉴가 아닙니다");
        }

        // 도메인 검증
        campaignDomainService.validateTargetFeedbackCount(request.getTargetFeedbackCount());
        campaignDomainService.validateCampaignCreation(foodItem, request.getStartDate(), request.getEndDate());

        // 캠페인 생성
        Campaign campaign = Campaign.builder()
            .name(request.getName())
            .description(request.getDescription())
            .store(store)
            .foodItem(foodItem)
            .targetFeedbackCount(request.getTargetFeedbackCount())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .status(Campaign.CampaignStatus.DRAFT)
            .build();

        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("캠페인 생성 완료: campaignId={}", savedCampaign.getId());

        return savedCampaign.getId();
    }

    public void updateCampaign(Long campaignId, UpdateCampaignRequest request, Long userId) {
        log.info("캠페인 수정 요청: campaignId={}, userId={}", campaignId, userId);

        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.USER_NOT_FOUND));

        UserStoreRole usr = userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(userId, request.getStoreId())
                .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_ACCESS_DENIED));

        // 캠페인 관리 권한 확인
        if (!usr.canManageStore()) {
            throw new CampaignException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        // 도메인 검증
        campaignDomainService.validateTargetFeedbackCount(request.getTargetFeedbackCount());
        campaignDomainService.validateCampaignUpdate(campaign, request.getStartDate(), request.getEndDate());

        // 업데이트
        campaign.updateCampaign(
            request.getName(),
            request.getDescription(),
            request.getTargetFeedbackCount(),
            request.getStartDate(),
            request.getEndDate()
        );

        campaignRepository.save(campaign);
        log.info("캠페인 수정 완료: campaignId={}", campaignId);
    }

    public void deleteCampaign(Long campaignId, Long userId) {
        log.info("캠페인 삭제 요청: campaignId={}, userId={}", campaignId, userId);

        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.USER_NOT_FOUND));

        // UserStoreRole로 권한 확인
        UserStoreRole usr = userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(userId, campaign.getStore().getId())
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_ACCESS_DENIED));

        // 캠페인 관리 권한 확인
        if (!usr.canManageStore()) {
            throw new CampaignException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        // 활성 캠페인은 삭제 불가
        if (campaign.getStatus() == Campaign.CampaignStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 캠페인은 삭제할 수 없습니다");
        }

        // Soft delete
        campaign.softDelete();
        campaignRepository.save(campaign);
        log.info("캠페인 삭제 완료: campaignId={}", campaignId);
    }

    public void changeCampaignStatus(Long campaignId, Campaign.CampaignStatus newStatus, Long userId) {
        log.info("캠페인 상태 변경 요청: campaignId={}, newStatus={}, userId={}",
            campaignId, newStatus, userId);

        Campaign campaign = campaignRepository.findById(campaignId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.CAMPAIGN_NOT_FOUND));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CampaignException(ErrorMessage.USER_NOT_FOUND));

        // UserStoreRole로 권한 확인
        UserStoreRole usr = userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(userId, campaign.getStore().getId())
            .orElseThrow(() -> new CampaignException(ErrorMessage.STORE_ACCESS_DENIED));

        // 캠페인 관리 권한 확인
        if (!usr.canManageStore()) {
            throw new CampaignException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        // 상태 변경
        switch (newStatus) {
            case ACTIVE -> campaign.activate();
            case PAUSED -> campaign.pause();
            case COMPLETED -> campaign.complete();
            case EXPIRED -> campaign.expire();
            default -> throw new IllegalArgumentException("유효하지 않은 상태입니다: " + newStatus);
        }

        campaignRepository.save(campaign);
        log.info("캠페인 상태 변경 완료: campaignId={}, newStatus={}", campaignId, newStatus);
    }
}