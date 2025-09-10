package com.example.chalpuplatform.fooditem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "음식 생성/수정 요청")
public class FoodItemRequest {
    
    @Schema(description = "음식명", example = "김치찌개", required = true)
    private String foodName;

    @Schema(description = "썸네일 url", example = "cdn.chalpu.com/food-thumbnail.jpg")
    private String thumbnailUrl;
    
    @Schema(description = "가격", example = "8000")
    private BigDecimal price;
    
    @Schema(description = "음식 활성화 여부")
    @Builder.Default
    private Boolean isActive = true;
} 