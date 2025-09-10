package com.example.chalpuplatform.reward.repository;

import com.example.chalpuplatform.reward.domain.RewardRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {

    List<RewardRedemption> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<RewardRedemption> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, RewardRedemption.RedemptionStatus status);
    
    @Query("SELECT COUNT(rr) FROM RewardRedemption rr WHERE rr.user.id = :userId AND rr.status = 'ISSUED'")
    int countActiveRedemptionsByUserId(@Param("userId") Long userId);
    
    boolean existsByUserIdAndRewardIdAndStatus(Long userId, Long rewardId, RewardRedemption.RedemptionStatus status);
}