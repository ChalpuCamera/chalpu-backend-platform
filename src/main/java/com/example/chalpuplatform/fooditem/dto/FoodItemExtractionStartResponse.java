package com.example.chalpuplatform.fooditem.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemExtractionStartResponse {
    private String requestId;
    private String message;

    public static FoodItemExtractionStartResponse of(String requestId) {
        return FoodItemExtractionStartResponse.builder()
                .requestId(requestId)
                .message("메뉴 추출이 시작되었습니다. requestId로 진행 상태를 확인하세요.")
                .build();
    }
}