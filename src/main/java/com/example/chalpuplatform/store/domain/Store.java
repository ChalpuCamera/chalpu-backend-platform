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

    @Column(name = "site_link", unique = true, nullable = false, length = 100)
    private String siteLink;

    @Builder.Default
    @Column(name = "required_stamps_for_coupon", nullable = false)
    private Integer requiredStampsForCoupon = 10;

    @Builder.Default
    @Column(name = "display_template",nullable = false)
    private Integer displayTemplate = 1;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Builder.Default
    @JsonManagedReference
    private List<FoodItem> foodItems = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StoreLink> links = new ArrayList<>();

    @Column(name = "auto_create_menus")
    @Builder.Default
    private Boolean autoCreateMenus = false;

    public static Store createStore(StoreRequest storeRequest){
        Store store = Store.builder()
                .storeName(storeRequest.getStoreName())
                .address(storeRequest.getAddress())
                .description(storeRequest.getDescription())
                .siteLink(storeRequest.getSiteLink())
                .build();

        if (storeRequest.getLinks() != null && !storeRequest.getLinks().isEmpty()) {
            for (int i = 0; i < storeRequest.getLinks().size(); i++) {
                StoreLink link = StoreLink.create(
                        store,
                        storeRequest.getLinks().get(i).getLinkType(),
                        storeRequest.getLinks().get(i).getCustomLabel(),
                        storeRequest.getLinks().get(i).getUrl(),
                        storeRequest.getLinks().get(i).getIsVisible(),
                        i
                );
                store.getLinks().add(link);
            }
        }

        return store;
    }

    public void updateStore(StoreRequest storeRequest) {
        this.storeName = storeRequest.getStoreName();
        this.address = storeRequest.getAddress();
        this.description = storeRequest.getDescription();
        this.siteLink = storeRequest.getSiteLink();
        this.requiredStampsForCoupon = storeRequest.getRequiredStampsForCoupon() != null ? storeRequest.getRequiredStampsForCoupon() : this.requiredStampsForCoupon;
        this.displayTemplate = storeRequest.getDisplayTemplate() != null ? storeRequest.getDisplayTemplate() : this.displayTemplate;
        this.autoCreateMenus = storeRequest.getAutoCreateMenus() != null ? storeRequest.getAutoCreateMenus() : false;

        if (storeRequest.getLinks() != null) {
            this.links.clear();

            for (int i = 0; i < storeRequest.getLinks().size(); i++) {
                StoreLink link = StoreLink.create(
                        this,
                        storeRequest.getLinks().get(i).getLinkType(),
                        storeRequest.getLinks().get(i).getCustomLabel(),
                        storeRequest.getLinks().get(i).getUrl(),
                        storeRequest.getLinks().get(i).getIsVisible(),
                        i
                );
                this.links.add(link);
            }
        }
    }

    public void softDelete() {
        this.isActive = false;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
} 