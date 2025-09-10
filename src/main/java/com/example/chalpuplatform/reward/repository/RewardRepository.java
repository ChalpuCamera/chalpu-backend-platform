package com.example.chalpuplatform.reward.repository;

import com.example.chalpuplatform.reward.domain.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByRewardType(String rewardType);
    
    @Query("SELECT r FROM Reward r WHERE r.expiryDate IS NULL OR r.expiryDate >= :currentDate")
    List<Reward> findAvailableRewards(LocalDate currentDate);
    
    @Query("SELECT r FROM Reward r WHERE r.rewardType = :rewardType AND (r.expiryDate IS NULL OR r.expiryDate >= :currentDate)")
    List<Reward> findAvailableRewardsByType(String rewardType, LocalDate currentDate);
}