package com.example.chalpuplatform.photo.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import jakarta.persistence.*;
import lombok.*;

@NamedEntityGraph(
    name = "Photo.withFoodItem",
    attributeNodes = {
        @NamedAttributeNode("foodItem")
    }
)
@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Photo extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private FoodItem foodItem;

    @Column(length = 500, nullable = false, unique = true)
    private String s3Key;

    @Column(length = 255, nullable = false)
    private String fileName;

    private String filter;
    private Integer fileSize;
    private Integer imageWidth;
    private Integer imageHeight;

    @Builder.Default
    private Boolean isActive = true;

    public void softDelete() {
        this.isActive = false;
    }
}