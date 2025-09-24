package com.example.chalpuplatform.campaign.repository;

import com.example.chalpuplatform.campaign.domain.Campaign;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    // 매장별 캠페인 조회
    Page<Campaign> findByStoreAndIsActiveTrue(Store store, Pageable pageable);

    // 매장의 특정 상태 캠페인 조회 (페이징)
    Page<Campaign> findByStoreAndStatusAndIsActiveTrue(Store store, Campaign.CampaignStatus status, Pageable pageable);

    // 특정 음식의 활성 캠페인 조회
    @Query("SELECT c FROM Campaign c " +
           "WHERE c.foodItem = :foodItem " +
           "AND c.status = 'ACTIVE' " +
           "AND c.isActive = true " +
           "AND c.startDate <= :now " +
           "AND c.endDate > :now")
    Optional<Campaign> findActiveCampaignByFoodItem(
        @Param("foodItem") FoodItem foodItem,
        @Param("now") LocalDateTime now
    );

    // 매장의 활성 캠페인 조회
    @Query("SELECT c FROM Campaign c " +
           "WHERE c.store = :store " +
           "AND c.status = 'ACTIVE' " +
           "AND c.isActive = true " +
           "AND c.startDate <= :now " +
           "AND c.endDate > :now")
    List<Campaign> findActiveCampaignsByStore(
        @Param("store") Store store,
        @Param("now") LocalDateTime now
    );

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

    // 종료일이 지났지만 아직 EXPIRED 처리되지 않은 캠페인
    @Query("SELECT c FROM Campaign c " +
           "WHERE c.status IN ('ACTIVE', 'PAUSED') " +
           "AND c.endDate < :now")
    List<Campaign> findCampaignsToExpire(@Param("now") LocalDateTime now);

    // 매장과 ID로 캠페인 조회 (권한 확인용)
    Optional<Campaign> findByIdAndStore(Long id, Store store);

    // 활성 캠페인 수 조회
    @Query("SELECT COUNT(c) FROM Campaign c " +
           "WHERE c.store = :store " +
           "AND c.status = 'ACTIVE' " +
           "AND c.isActive = true")
    long countActiveByStore(@Param("store") Store store);
}