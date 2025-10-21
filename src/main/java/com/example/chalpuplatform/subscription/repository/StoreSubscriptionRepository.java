package com.example.chalpuplatform.subscription.repository;

import com.example.chalpuplatform.subscription.domain.StoreSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreSubscriptionRepository extends JpaRepository<StoreSubscription, Long> {

    Optional<StoreSubscription> findByUserIdAndStoreId(Long userId, Long storeId);

    boolean existsByUserIdAndStoreIdAndIsActiveTrue(Long userId, Long storeId);

    Page<StoreSubscription> findByStoreIdAndIsActiveTrueAndNotificationEnabledTrue(
            Long storeId,
            Pageable pageable
    );

    @Query("SELECT s FROM StoreSubscription s " +
           "WHERE s.storeId = :storeId " +
           "AND s.isActive = true " +
           "AND s.notificationEnabled = true")
    List<StoreSubscription> findActiveSubscribersByStoreId(@Param("storeId") Long storeId);

    long countByStoreIdAndIsActiveTrueAndNotificationEnabledTrue(Long storeId);
}
