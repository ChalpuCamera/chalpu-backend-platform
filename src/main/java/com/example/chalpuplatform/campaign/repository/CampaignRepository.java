package com.example.chalpuplatform.campaign.repository;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // 매장별 캠페인 조회
    @EntityGraph("Campaign.withStoreAndFoodItem")
    Page<Campaign> findByStoreAndIsActiveTrue(Store store, Pageable pageable);

    // 매장의 특정 상태 캠페인 조회 (페이징)
    @EntityGraph("Campaign.withStoreAndFoodItem")
    Page<Campaign> findByStoreAndStatusAndIsActiveTrue(Store store, Campaign.CampaignStatus status, Pageable pageable);

    // 특정 음식에 대한 중복 활성 캠페인 확인
    @Query("SELECT COUNT(c) > 0 FROM Campaign c " +
           "WHERE c.foodItem = :foodItem " +
           "AND c.status IN ('ACTIVE', 'PAUSED') " +
           "AND c.isActive = true " +
           "AND c.id != :excludeId " +
           "AND ((c.startDate <= :endDate AND c.endDate >= :startDate))")
    boolean existsOverlappingCampaign(
        @Param("foodItem") FoodItem foodItem,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("excludeId") Long excludeId
    );

    // 피드백 카운트 원자적 증가
    @Modifying
    @Query("UPDATE Campaign c SET c.currentFeedbackCount = c.currentFeedbackCount + 1 WHERE c.id = :campaignId")
    int incrementFeedbackCount(@Param("campaignId") Long campaignId);

    // 특정 매장과 음식에 대한 활성 캠페인 찾기
    @Query("SELECT c FROM Campaign c " +
           "WHERE c.store = :store " +
           "AND c.foodItem = :foodItem " +
           "AND c.status = 'ACTIVE' " +
           "AND c.isActive = true " +
           "AND c.startDate <= :currentTime " +
           "AND c.endDate >= :currentTime")
    Optional<Campaign> findActiveByStoreAndFoodItem(
        @Param("store") Store store,
        @Param("foodItem") FoodItem foodItem,
        @Param("currentTime") LocalDateTime currentTime
    );
}