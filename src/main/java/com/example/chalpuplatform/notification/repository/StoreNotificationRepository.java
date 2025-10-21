package com.example.chalpuplatform.notification.repository;

import com.example.chalpuplatform.notification.domain.NotificationStatus;
import com.example.chalpuplatform.notification.domain.StoreNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StoreNotificationRepository extends JpaRepository<StoreNotification, Long> {

    Page<StoreNotification> findByStoreIdOrderByCreatedAtDesc(Long storeId, Pageable pageable);

    List<StoreNotification> findByStatusAndScheduledAtBefore(
            NotificationStatus status,
            LocalDateTime scheduledAt
    );
}
