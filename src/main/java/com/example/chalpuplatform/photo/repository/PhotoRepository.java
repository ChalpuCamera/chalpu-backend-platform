package com.example.chalpuplatform.photo.repository;

import com.example.chalpuplatform.photo.domain.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    @Query("SELECT p FROM Photo p WHERE p.foodItem.id = :foodId AND p.isActive = true")
    Page<Photo> findByFoodItemIdAndIsActiveTrueWithoutJoin(@Param("foodId") Long foodId, Pageable pageable);

    // 경량화된 조회 메서드 (연관 엔티티 조회 없음)
    @Query("SELECT p FROM Photo p WHERE p.id = :id AND p.isActive = true")
    Optional<Photo> findByIdAndIsActiveTrueWithoutJoin(@Param("id") Long id);

    /**
     * FoodItem 삭제 시 연관된 Photo들 소프트 딜리트
     * @param foodItemId 음식 아이템 ID
     */
    @Modifying
    @Query("UPDATE Photo p SET p.isActive = false WHERE p.foodItem.id = :foodItemId")
    void softDeleteByFoodItemId(@Param("foodItemId") Long foodItemId);
    
    /**
     * 여러 FoodItem 삭제 시 연관된 Photo들 벌크 소프트 딜리트
     * @param foodItemIds 음식 아이템 ID 리스트
     */
    @Modifying
    @Query("UPDATE Photo p SET p.isActive = false WHERE p.foodItem.id IN :foodItemIds")
    void softDeleteByFoodItemIds(@Param("foodItemIds") List<Long> foodItemIds);

    /**
     * Store에 속한 모든 Photo 조회 (FoodItem과 Store를 fetch join)
     * @param storeId 스토어 ID
     * @param pageable 페이징 정보
     */
    @Query("SELECT p FROM Photo p JOIN FETCH p.foodItem fi JOIN FETCH fi.store WHERE fi.store.id = :storeId AND p.isActive = true")
    Page<Photo> findByStoreIdWithFetchJoin(@Param("storeId") Long storeId, Pageable pageable);
    
    /**
     * Store에 속한 모든 Photo 조회 (FoodItem을 통한 간접 접근)
     * @param storeId 스토어 ID
     */
    @Query("SELECT p FROM Photo p JOIN p.foodItem fi WHERE fi.store.id = :storeId AND p.isActive = true")
    List<Photo> findByStoreIdAndIsActiveTrueWithoutJoin(@Param("storeId") Long storeId);

    /**
     * Store와 FoodItem을 모두 fetch join하여 Photo 조회
     * @param photoId 사진 ID
     */
    @EntityGraph(attributePaths = {"foodItem", "foodItem.store"})
    @Query("SELECT p FROM Photo p WHERE p.id = :photoId AND p.isActive = true")
    Optional<Photo> findByIdWithFoodItemAndStore(@Param("photoId") Long photoId);
} 