package com.example.chalpuplatform.fooditem.repository;

import com.example.chalpuplatform.fooditem.domain.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    // Fetch Join 없이 조회하기 위한 새로운 메서드
    @Query("SELECT fi FROM FoodItem fi WHERE fi.store.id = :storeId AND fi.isActive = true")
    Page<FoodItem> findByStoreIdAndIsActiveTrueWithoutJoin(@Param("storeId") Long storeId, Pageable pageable);

    // 경량화된 조회 메서드 (연관 엔티티 조회 없음)
    @Query("SELECT fi FROM FoodItem fi WHERE fi.id = :id AND fi.isActive = true")
    Optional<FoodItem> findByIdAndIsActiveTrueWithoutJoin(@Param("id") Long id);

    // 권한 검증용 - storeId만 조회
    @Query("SELECT fi.store.id FROM FoodItem fi WHERE fi.id = :id AND fi.isActive = true")
    Optional<Long> findStoreIdByFoodItemId(@Param("id") Long id);

    // 메뉴 추출을 위한 중복 체크용 메서드
    @Query("SELECT fi FROM FoodItem fi WHERE fi.store.id = :storeId AND fi.foodName = :foodName AND fi.isActive = true")
    Optional<FoodItem> findByStoreIdAndFoodName(@Param("storeId") Long storeId, @Param("foodName") String foodName);
} 