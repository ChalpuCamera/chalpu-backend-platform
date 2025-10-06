package com.example.chalpuplatform.store.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.dto.StoreRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Store extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(length = 100, nullable = false)
    private String storeName;

    @Column(name = "address")
    private String address;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Schema(description = "가게 설명",nullable = true)
    private String description;

    @Builder.Default
    @Column(name = "feedback_count", nullable = false)
    private Long feedbackCount = 0L;

    @Builder.Default
    @Column(name = "menu_count", nullable = false)
    private Long menuCount = 0L;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Embedded
    private DeliveryPlatformLinks deliveryPlatformLinks;
    
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    @JsonManagedReference
    private List<FoodItem> foodItems = new ArrayList<>();

    public static Store createStore(StoreRequest storeRequest){
        return Store.builder()
                .storeName(storeRequest.getStoreName())
                .address(storeRequest.getAddress())
                .description(storeRequest.getDescription())
                .deliveryPlatformLinks(DeliveryPlatformLinks.builder()
                        .baeminLink(storeRequest.getBaeminLink())
                        .yogiyoLink(storeRequest.getYogiyoLink())
                        .coupangeatsLink(storeRequest.getCoupangeatsLink())
                        .build())
                .build();
    }

    public void updateStore(StoreRequest storeRequest) {
        this.storeName = storeRequest.getStoreName();
        this.address = storeRequest.getAddress();
        this.description = storeRequest.getDescription();

        if (this.deliveryPlatformLinks == null) {
            this.deliveryPlatformLinks = new DeliveryPlatformLinks();
        }
        this.deliveryPlatformLinks.setBaeminLink(storeRequest.getBaeminLink());
        this.deliveryPlatformLinks.setYogiyoLink(storeRequest.getYogiyoLink());
        this.deliveryPlatformLinks.setCoupangeatsLink(storeRequest.getCoupangeatsLink());
    }

    public void softDelete() {
        this.isActive = false;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
} 