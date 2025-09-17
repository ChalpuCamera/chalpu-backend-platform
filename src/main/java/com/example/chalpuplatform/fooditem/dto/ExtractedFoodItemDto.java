package com.example.chalpuplatform.fooditem.dto;

import com.example.chalpuplatform.fooditem.domain.FoodCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedFoodItemDto {
    private String name;
    private Integer price;
    private String description;
    private FoodCategory category;
}