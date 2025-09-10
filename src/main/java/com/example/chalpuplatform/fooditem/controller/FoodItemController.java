package com.example.chalpuplatform.fooditem.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.fooditem.dto.FoodItemRequest;
import com.example.chalpuplatform.fooditem.dto.FoodItemResponse;
import com.example.chalpuplatform.fooditem.service.FoodItemService;
import com.example.chalpuplatform.oauth.security.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Tag(name = "FoodItem", description = "음식 관련 API")
public class FoodItemController {

    private final FoodItemService foodItemService;

    @GetMapping("/store/{storeId}")
    @Operation(
        summary = "매장별 음식 목록 조회",
        description = "특정 매장의 음식 목록을 페이지네이션하여 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ApiResponse<PageResponse<FoodItemResponse>> getFoodItems(
            @PathVariable @Parameter(description = "매장 ID") Long storeId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<FoodItemResponse> foodItems = foodItemService.getFoodItems(storeId, pageable);
        return ApiResponse.success(foodItems);
    }

    @GetMapping("/{foodId}")
    @Operation(
        summary = "음식 상세 조회",
        description = "특정 음식의 상세 정보를 조회합니다."
    )
    public ApiResponse<FoodItemResponse> getFoodItem(
            @PathVariable @Parameter(description = "음식 ID") Long foodId) {
        FoodItemResponse foodItem = foodItemService.getFoodItem(foodId);
        return ApiResponse.success(foodItem);
    }

    @PostMapping("/store/{storeId}")
    @Operation(
        summary = "음식 생성",
        description = "새로운 음식을 생성합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ApiResponse<FoodItemResponse> createFoodItem(
            @PathVariable @Parameter(description = "매장 ID") Long storeId,
            @RequestBody FoodItemRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        FoodItemResponse foodItem = foodItemService.createFoodItem(storeId, request, userDetails.getId());
        return ApiResponse.success(foodItem);
    }

    @PutMapping("/{foodId}")
    @Operation(
        summary = "음식 수정",
        description = "기존 음식 정보를 수정합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ApiResponse<FoodItemResponse> updateFoodItem(
            @PathVariable @Parameter(description = "음식 ID") Long foodId,
            @RequestBody FoodItemRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        FoodItemResponse foodItem = foodItemService.updateFoodItem(foodId, request, userDetails.getId());
        return ApiResponse.success(foodItem);
    }

    @DeleteMapping("/{foodId}")
    @Operation(
        summary = "음식 삭제",
        description = "음식을 삭제합니다 (소프트 딜리트).",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    public ApiResponse<Void> deleteFoodItem(
            @PathVariable @Parameter(description = "음식 ID") Long foodId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        foodItemService.deleteFoodItem(foodId, userDetails.getId());
        return ApiResponse.success();
    }

    @GetMapping("/store/{storeId}/search")
    @Operation(
        summary = "음식 검색",
        description = "매장 내에서 음식명으로 검색합니다."
    )
    public ApiResponse<PageResponse<FoodItemResponse>> searchFoodItems(
            @PathVariable @Parameter(description = "매장 ID") Long storeId,
            @RequestParam @Parameter(description = "검색 키워드") String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<FoodItemResponse> foodItems = foodItemService.searchFoodItems(storeId, keyword, pageable);
        return ApiResponse.success(foodItems);
    }
} 