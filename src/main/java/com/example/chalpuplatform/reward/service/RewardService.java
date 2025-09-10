package com.example.chalpuplatform.reward.service;

import com.example.chalpuplatform.common.exception.UserException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.RewardException;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.domain.UserProfile;
import com.example.chalpuplatform.user.repository.UserRepository;
import com.example.chalpuplatform.reward.domain.Reward;
import com.example.chalpuplatform.reward.domain.RewardRedemption;
import com.example.chalpuplatform.reward.dto.RewardRedemptionRequest;
import com.example.chalpuplatform.reward.dto.RewardRedemptionResponse;
import com.example.chalpuplatform.reward.dto.RewardResponse;
import com.example.chalpuplatform.reward.repository.RewardRedemptionRepository;
import com.example.chalpuplatform.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RewardService {

    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RewardResponse> getAvailableRewards() {
        List<Reward> rewards = rewardRepository.findAvailableRewards(LocalDate.now());
        return rewards.stream()
                .map(RewardResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> getAvailableRewardsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        List<Reward> allRewards = rewardRepository.findAvailableRewards(LocalDate.now());

        return allRewards.stream()
                .filter(reward -> canUserAffordReward(user, reward))
                .map(RewardResponse::from)
                .collect(Collectors.toList());
    }

    private boolean canUserAffordReward(User user, Reward reward) {
        if (user.getUserProfile() == null) {
            return false;
        }
        Integer userRewardCount = user.getUserProfile().getRewardCount();
        Integer requiredCount = reward.getRequiredCount();
        
        return userRewardCount != null && requiredCount != null && userRewardCount >= requiredCount;
    }

    public RewardRedemptionResponse redeemReward(Long userId, RewardRedemptionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        if (user.getUserProfile() == null) {
            throw new RewardException(ErrorMessage.INSUFFICIENT_REWARD_COUNT);
        }

        Reward reward = rewardRepository.findById(request.getRewardId())
                .orElseThrow(() -> new RewardException(ErrorMessage.REWARD_NOT_FOUND));

        if (!reward.isAvailable()) {
            throw new RewardException(ErrorMessage.REWARD_NOT_AVAILABLE);
        }

        if (!canUserAffordReward(user, reward)) {
            throw new RewardException(ErrorMessage.INSUFFICIENT_REWARD_COUNT);
        }

        // 리워드 횟수 차감
        try {
            user.getUserProfile().decrementRewardCount(reward.getRequiredCount());
        } catch (IllegalArgumentException e) {
            throw new RewardException(ErrorMessage.INSUFFICIENT_REWARD_COUNT);
        }

        RewardRedemption redemption = RewardRedemption.createRedemption(
                user, reward, user.getUserProfile().getRewardCount());

        RewardRedemption savedRedemption = redemptionRepository.save(redemption);

        log.info("리워드 교환 완료: userId={}, rewardId={}, required_count={}, remaining_count={}", 
                userId, request.getRewardId(), reward.getRequiredCount(), 
                user.getUserProfile().getRewardCount());

        return RewardRedemptionResponse.from(savedRedemption);
    }

    @Transactional(readOnly = true)
    public List<RewardRedemptionResponse> getUserRedemptions(Long userId) {
        List<RewardRedemption> redemptions = redemptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        return redemptions.stream()
                .map(RewardRedemptionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RewardRedemptionResponse> getActiveRedemptions(Long userId) {
        List<RewardRedemption> redemptions = redemptionRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(
                        userId, RewardRedemption.RedemptionStatus.ISSUED);

        return redemptions.stream()
                .map(RewardRedemptionResponse::from)
                .collect(Collectors.toList());
    }

    public void markRedemptionAsUsed(Long redemptionId) {
        RewardRedemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new RewardException(ErrorMessage.REWARD_REDEMPTION_NOT_FOUND));

        redemption.markAsUsed();
        log.info("리워드 사용 처리 완료: redemptionId={}", redemptionId);
    }

    public void cancelRedemption(Long redemptionId) {
        RewardRedemption redemption = redemptionRepository.findById(redemptionId)
                .orElseThrow(() -> new RewardException(ErrorMessage.REWARD_REDEMPTION_NOT_FOUND));

        redemption.cancel();
        log.info("리워드 사용 취소 완료: redemptionId={}", redemptionId);
    }

    @Transactional(readOnly = true)
    public boolean isEligibleForReward(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        if (user.getUserProfile() == null) {
            return false;
        }
        
        return user.getUserProfile().getRewardCount() != null && user.getUserProfile().getRewardCount() > 0;
    }
}