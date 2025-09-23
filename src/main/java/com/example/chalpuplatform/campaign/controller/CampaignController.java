package com.example.chalpuplatform.campaign.controller;

import com.example.chalpuplatform.campaign.dto.*;
import com.example.chalpuplatform.campaign.service.CampaignCommandService;
import com.example.chalpuplatform.campaign.service.CampaignQueryService;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaign", description = "캠페인 관리 API")
public class CampaignController {

    private final CampaignCommandService campaignCommandService;
    private final CampaignQueryService campaignQueryService;

    // Command operations
    @PostMapping
    @Operation(summary = "캠페인 생성", description = "새로운 캠페인을 생성합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "캠페인 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장 또는 음식을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Map<String, Long>>> createCampaign(
        @Valid @RequestBody CreateCampaignRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long campaignId = campaignCommandService.createCampaign(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(Map.of("campaignId", campaignId)));
    }

    @PutMapping
    @Operation(summary = "캠페인 수정", description = "기존 캠페인 정보를 수정합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "캠페인 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> updateCampaign(
        @Valid @RequestBody UpdateCampaignRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        campaignCommandService.updateCampaign(request.getCampaignId(), request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping
    @Operation(summary = "캠페인 삭제", description = "캠페인을 소프트 삭제합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "캠페인 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "활성 상태 캠페인은 삭제 불가"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(
        @Valid @RequestBody DeleteCampaignRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        campaignCommandService.deleteCampaign(request.getCampaignId(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PatchMapping("/status")
    @Operation(summary = "캠페인 상태 변경", description = "캠페인의 상태를 변경합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "캠페인 상태 변경 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 상태"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Void>> changeCampaignStatus(
        @Valid @RequestBody ChangeCampaignStatusRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        campaignCommandService.changeCampaignStatus(request.getCampaignId(), request.getStatus(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // Query operations
    @GetMapping("/{id}")
    @Operation(summary = "캠페인 상세 조회", description = "캠페인의 상세 정보를 조회합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "캠페인 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CampaignDetailResponse>> getCampaign(
        @PathVariable("id") Long campaignId
    ) {
        CampaignDetailResponse response = campaignQueryService.getCampaignById(campaignId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}")
    @Operation(summary = "매장별 캠페인 목록 조회", description = "특정 매장의 캠페인 목록을 페이징하여 조회합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "캠페인 목록 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<PageResponse<CampaignResponse>>> getCampaignsByStore(
        @PathVariable("storeId") Long storeId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        PageResponse<CampaignResponse> response = campaignQueryService.getCampaignsByStore(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}/active")
    @Operation(summary = "매장의 활성 캠페인 조회", description = "특정 매장의 활성 상태 캠페인만 조회합니다")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getActiveCampaignsByStore(
        @PathVariable("storeId") Long storeId
    ) {
        List<CampaignResponse> response = campaignQueryService.getActiveCampaignsByStore(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/statistics")
    @Operation(summary = "캠페인 통계 조회", description = "캠페인의 상세 통계 정보를 조회합니다")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "통계 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "캠페인을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<CampaignStatisticsResponse>> getCampaignStatistics(
        @PathVariable("id") Long campaignId
    ) {
        CampaignStatisticsResponse response = campaignQueryService.getCampaignStatistics(campaignId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/store/{storeId}/dashboard")
    @Operation(summary = "매장 캠페인 대시보드", description = "매장의 캠페인 대시보드 데이터를 조회합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCampaignDashboard(
        @PathVariable("storeId") Long storeId
    ) {
        Map<String, Object> response = campaignQueryService.getDashboardData(storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}