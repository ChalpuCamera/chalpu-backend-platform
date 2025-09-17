package com.example.chalpuplatform.customerfeedback.repository;

import com.example.chalpuplatform.customerfeedback.domain.CustomerFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {

    List<CustomerFeedback> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
    
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
        LEFT JOIN FETCH u.userProfile up
        WHERE cf.id = :feedbackId AND cf.isActive = true
    """)
    CustomerFeedback findByIdWithUserProfile(@Param("feedbackId") Long feedbackId);
}