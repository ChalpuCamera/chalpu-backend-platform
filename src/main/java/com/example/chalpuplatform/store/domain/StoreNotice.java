package com.example.chalpuplatform.store.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreNotice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_representative")
    private Boolean isRepresentative;

    @Builder
    public StoreNotice(Long storeId, String title, String body, Boolean isRepresentative) {
        this.storeId = storeId;
        this.title = title;
        this.body = body;
        this.isRepresentative = isRepresentative != null ? isRepresentative : false;
    }

    public void update(String title, String body,Boolean isRepresentative) {
        this.title = title;
        this.body = body;
        this.isRepresentative = isRepresentative != null ? isRepresentative : this.isRepresentative;
    }

    public void makeRepresentative(){
        this.isRepresentative = true;
    }

    public boolean belongsToStore(Long storeId){
        return this.getStoreId().equals(storeId);
    }
}
