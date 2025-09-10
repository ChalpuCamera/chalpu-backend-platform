package com.example.chalpuplatform.store.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.dto.StoreRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
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
    
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    private List<FoodItem> foodItems = new ArrayList<>();

    public static Store createStore(StoreRequest storeRequest){
        return Store.builder()
                .storeName(storeRequest.getStoreName())
                .address(storeRequest.getAddress())
                .build();
    }

    public void updateStore(StoreRequest storeRequest) {
        this.storeName = storeRequest.getStoreName();
        this.address = storeRequest.getAddress();
    }

    public void softDelete() {
        this.isActive = false;
    }
} 