package com.example.chalpuplatform.customerfeedback.repository;

import com.example.chalpuplatform.customerfeedback.domain.CustomerFeedback;
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

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    
    Page<CustomerFeedback> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<CustomerFeedback> findByStoreIdAndIsActiveTrueOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    Page<CustomerFeedback> findByFoodItemIdAndIsActiveTrueOrderByCreatedAtDesc(Long foodItemId, Pageable pageable);

    // 매장의 모든 음식별 읽지 않은 피드백 개수 조회
    @Query("""
        SELECT f.id as foodItemId, f.foodName as foodName,
               COUNT(CASE WHEN cf.isViewed = false THEN 1 END) as unreadCount,
               COUNT(cf) as totalCount
        FROM FoodItem f
        LEFT JOIN CustomerFeedback cf ON cf.foodItem = f AND cf.isActive = true
        WHERE f.store.id = :storeId AND f.isActive = true
        GROUP BY f.id, f.foodName
        ORDER BY unreadCount DESC, f.foodName ASC
    """)
    List<Object[]> findUnreadCountsByStoreId(@Param("storeId") Long storeId);

    // 특정 피드백과 관련 엔티티들을 함께 조회
    @Query("""
        SELECT cf FROM CustomerFeedback cf
        JOIN FETCH cf.user u
        WHERE cf.id = :feedbackId AND cf.isActive = true
    """)
    CustomerFeedback findByIdWithUserProfile(@Param("feedbackId") Long feedbackId);

    // 매장 피드백 조회 시 연관 엔티티 페치 조인
    @Query("""
        SELECT cf FROM CustomerFeedback cf
        LEFT JOIN FETCH cf.foodItem
        LEFT JOIN FETCH cf.store
        LEFT JOIN FETCH cf.user
        LEFT JOIN FETCH cf.survey
        WHERE cf.store.id = :storeId
        AND cf.isActive = true
        ORDER BY cf.createdAt DESC
    """)
    Page<CustomerFeedback> findByStoreIdWithDetails(@Param("storeId") Long storeId, Pageable pageable);

    // 사용자 피드백 조회 시 연관 엔티티 페치 조인
    @Query("""
        SELECT cf FROM CustomerFeedback cf
        LEFT JOIN FETCH cf.foodItem
        LEFT JOIN FETCH cf.store
        LEFT JOIN FETCH cf.survey
        WHERE cf.user.id = :userId
        AND cf.isActive = true
        ORDER BY cf.createdAt DESC
    """)
    Page<CustomerFeedback> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

    // 음식별 피드백 조회 시 연관 엔티티 페치 조인
    @Query("""
        SELECT cf FROM CustomerFeedback cf
        LEFT JOIN FETCH cf.foodItem
        LEFT JOIN FETCH cf.store
        LEFT JOIN FETCH cf.user u
        LEFT JOIN FETCH cf.survey
        WHERE cf.foodItem.id = :foodItemId
        AND cf.isActive = true
        ORDER BY cf.createdAt DESC
    """)
    Page<CustomerFeedback> findByFoodItemIdWithDetails(@Param("foodItemId") Long foodItemId, Pageable pageable);

    // 캠페인 조건에 맞는 피드백 수 조회
    @Query("""
        SELECT COUNT(cf) FROM CustomerFeedback cf
        WHERE cf.foodItem = :foodItem
        AND cf.store = :store
        AND cf.createdAt BETWEEN :startDate AND :endDate
        AND cf.isActive = true
    """)
    long countByFoodItemAndStoreBetweenDates(
        @Param("foodItem") FoodItem foodItem,
        @Param("store") Store store,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 캠페인 기간별 일별 피드백 수 집계
    @Query("""
        SELECT DATE(cf.createdAt) as date, COUNT(cf) as count
        FROM CustomerFeedback cf
        WHERE cf.foodItem = :foodItem
        AND cf.store = :store
        AND cf.createdAt BETWEEN :startDate AND :endDate
        AND cf.isActive = true
        GROUP BY DATE(cf.createdAt)
        ORDER BY date
    """)
    List<Object[]> findDailyFeedbackCounts(
        @Param("foodItem") FoodItem foodItem,
        @Param("store") Store store,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 캠페인 조건에 맞는 평균 만족도 조회
    @Query("""
        SELECT AVG(cf.overallSatisfaction) FROM CustomerFeedback cf
        WHERE cf.foodItem = :foodItem
        AND cf.store = :store
        AND cf.createdAt BETWEEN :startDate AND :endDate
        AND cf.isActive = true
        AND cf.overallSatisfaction IS NOT NULL
    """)
    Double findAverageSatisfactionForCampaign(
        @Param("foodItem") FoodItem foodItem,
        @Param("store") Store store,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}