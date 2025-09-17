package com.example.chalpuplatform.fooditem.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemExtractionStatusResponse {
    private String requestId;
    private String status;
    private Integer progressPercentage;
    private String currentStep;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}