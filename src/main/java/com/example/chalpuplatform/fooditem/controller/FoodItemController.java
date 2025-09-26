package com.example.chalpuplatform.fooditem.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.fooditem.dto.FoodItemRequest;
import com.example.chalpuplatform.fooditem.dto.FoodItemResponse;
import com.example.chalpuplatform.fooditem.dto.FoodItemExtractionStartResponse;
import com.example.chalpuplatform.fooditem.dto.FoodItemExtractionStatusResponse;
import com.example.chalpuplatform.fooditem.service.FoodItemService;
import com.example.chalpuplatform.fooditem.service.FoodItemExtractionService;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Tag(name = "FoodItem", description = "음식 관련 API")
public class FoodItemController {

    private final FoodItemService foodItemService;
    private final FoodItemExtractionService foodItemExtractionService;

    @GetMapping("/store/{storeId}")
    @Operation(
        summary = "매장별 음식 목록 조회",
        description = "특정 매장의 음식 목록을 페이지네이션하여 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PreAuthorize("hasRole('OWNER')")
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
    @PreAuthorize("hasRole('OWNER')")
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
    @PreAuthorize("hasRole('OWNER')")
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
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<Void> deleteFoodItem(
            @PathVariable @Parameter(description = "음식 ID") Long foodId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        foodItemService.deleteFoodItem(foodId, userDetails.getId());
        return ApiResponse.success();
    }

    @PostMapping(value = "/menu/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "메뉴 이미지에서 FoodItem 추출",
        description = "메뉴판 이미지를 업로드하여 AI로 메뉴 정보를 추출하고 FoodItem으로 저장합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<FoodItemExtractionStartResponse> startMenuExtraction(
            @RequestPart("image") MultipartFile image,
            @RequestParam("storeId") Long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("event=menu_extraction_request, user_id={}, store_id={}, file_name={}, file_size={}",
                userDetails.getId(), storeId, image.getOriginalFilename(), image.getSize());

        String requestId = foodItemExtractionService.startMenuExtraction(
                userDetails.getId(), storeId, image);
        return ApiResponse.success(FoodItemExtractionStartResponse.of(requestId));
    }

    @GetMapping("/menu/extract/status/{requestId}")
    @Operation(
        summary = "메뉴 추출 진행 상태 조회",
        description = "메뉴 추출 작업의 진행 상태를 조회합니다 (Polling용)",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<FoodItemExtractionStatusResponse> getExtractionStatus(
            @PathVariable("requestId") String requestId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FoodItemExtractionStatusResponse status =
                foodItemExtractionService.getExtractionStatus(userDetails.getId(), requestId);
        return ApiResponse.success(status);
    }

    @PutMapping("/{foodId}/thumbnail")
    @Operation(
        summary = "음식 대표 사진 설정",
        description = "특정 음식의 대표 사진 URL을 설정합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<FoodItemResponse> updateThumbnail(
            @PathVariable("foodId") @Parameter(description = "음식 ID") Long foodId,
            @RequestParam("photoUrl") @Parameter(description = "대표 사진 URL") String photoUrl,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FoodItemResponse response = foodItemService.updateThumbnailUrl(foodId, photoUrl, userDetails);
        return ApiResponse.success(response);
    }
} 