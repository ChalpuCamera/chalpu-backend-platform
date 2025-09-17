package com.example.chalpuplatform.fooditem.repository;

import com.example.chalpuplatform.fooditem.domain.MenuExtractionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuExtractionProgressRepository extends JpaRepository<MenuExtractionProgress, String> {

    Optional<MenuExtractionProgress> findByRequestId(String requestId);

    @Query("SELECT p FROM MenuExtractionProgress p WHERE p.storeId = :storeId ORDER BY p.createdAt DESC")
    List<MenuExtractionProgress> findByStoreIdOrderByStartedAtDesc(@Param("storeId") Long storeId);

    @Query("SELECT p FROM MenuExtractionProgress p WHERE p.status = :status")
    List<MenuExtractionProgress> findByStatus(@Param("status") MenuExtractionProgress.ExtractionStatus status);

    @Query("SELECT p FROM MenuExtractionProgress p WHERE p.status = 'PROCESSING' AND p.createdAt < :timeout")
    List<MenuExtractionProgress> findTimeoutProcessing(@Param("timeout") LocalDateTime timeout);

    @Query("SELECT COUNT(p) FROM MenuExtractionProgress p WHERE p.storeId = :storeId AND p.status = 'PROCESSING'")
    long countProcessingByStoreId(@Param("storeId") Long storeId);
}