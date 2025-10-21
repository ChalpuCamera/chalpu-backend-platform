package com.example.chalpuplatform.notification.repository;

import com.example.chalpuplatform.notification.domain.NotificationOutbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    Page<NotificationOutbox> findByNotificationIdAndIsProcessedFalse(
            Long notificationId,
            Pageable pageable
    );

    Page<NotificationOutbox> findByIsProcessedFalseAndCreatedAtBefore(
            LocalDateTime createdAt,
            Pageable pageable
    );

    @Query("SELECT DISTINCT o.notificationId FROM NotificationOutbox o " +
           "WHERE o.isProcessed = false " +
           "AND o.createdAt < :createdAt")
    List<Long> findDistinctNotificationIdsByIsProcessedFalseAndCreatedAtBefore(
            @Param("createdAt") LocalDateTime createdAt
    );

    long countByNotificationIdAndIsProcessedFalse(Long notificationId);
}
