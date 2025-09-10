package com.example.chalpuplatform.fooditem.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.fooditem.dto.FoodItemRequest;
import com.example.chalpuplatform.store.domain.Store;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NamedEntityGraph(name = "FoodItem.withStore", attributeNodes = @NamedAttributeNode("store"))
@Entity
@Table(name = "food_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class FoodItem extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    @JsonBackReference
    private Store store;

    @Column(length = 100, nullable = false)
    private String foodName;
    private BigDecimal price;
    private String thumbnailUrl;
    
    @Builder.Default
    private Boolean isActive = true;

    // 정적 팩토리 메서드
    public static FoodItem createFoodItem(Store store, FoodItemRequest request) {
        return FoodItem.builder()
                .store(store)
                .foodName(request.getFoodName())
                .price(request.getPrice())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    // 업데이트 메서드
    public void updateFoodItem(FoodItemRequest request) {
        this.foodName = request.getFoodName();
        this.thumbnailUrl = request.getThumbnailUrl();
        this.price = request.getPrice();
        if (request.getIsActive() != null) {
            this.isActive = request.getIsActive();
        }
    }

    // 소프트 딜리트
    public void softDelete() {
        this.isActive = false;
        // 연관된 엔티티들은 Repository를 통해 서비스 레이어에서 처리
    }
} 