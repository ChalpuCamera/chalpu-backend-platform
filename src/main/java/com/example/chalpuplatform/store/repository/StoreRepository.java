package com.example.chalpuplatform.store.repository;

import com.example.chalpuplatform.store.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    Optional<Store> findByIdAndIsActiveTrue(Long id);

    List<Store> findByIsActiveTrue();

    Page<Store> findByIsActiveTrue(Pageable pageable);

    @Modifying
    @Query("UPDATE Store s SET s.feedbackCount = s.feedbackCount + 1 WHERE s.id = :storeId")
    void incrementFeedbackCount(@Param("storeId") Long storeId);

    @Modifying
    @Query("UPDATE Store s SET s.feedbackCount = s.feedbackCount - 1 WHERE s.id = :storeId AND s.feedbackCount > 0")
    void decrementFeedbackCount(@Param("storeId") Long storeId);

    @Modifying
    @Query("UPDATE Store s SET s.menuCount = s.menuCount + 1 WHERE s.id = :storeId")
    void incrementMenuCount(@Param("storeId") Long storeId);

    @Modifying
    @Query("UPDATE Store s SET s.menuCount = s.menuCount - 1 WHERE s.id = :storeId AND s.menuCount > 0")
    void decrementMenuCount(@Param("storeId") Long storeId);

    Optional<Store> findByStoreName(String storeName);
} 