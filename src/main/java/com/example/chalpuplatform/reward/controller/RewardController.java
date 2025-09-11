package com.example.chalpuplatform.reward.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.reward.dto.RewardRedemptionRequest;
import com.example.chalpuplatform.reward.dto.RewardRedemptionResponse;
import com.example.chalpuplatform.reward.dto.RewardResponse;
import com.example.chalpuplatform.reward.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Tag(name = "리워드", description = "고객 리워드 관리 API - 피드백 작성에 따른 리워드 생성, 교환, 사용 처리를 관리합니다")
public class RewardController {

    private final RewardService rewardService;

    @GetMapping
    @Operation(
        summary = "전체 리워드 목록 조회",
        description = "시스템에 등록된 모든 리워드 목록을 조회합니다. 각 리워드의 필요 피드백 수, 할인률, 설명 등을 확인할 수 있습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리워드 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": [{\"id\": 1, \"title\": \"10% 할인\", \"description\": \"다음 방문 시 10% 할인\", \"requiredFeedbackCount\": 5, \"discountRate\": 0.1, \"isActive\": true}]}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getAllRewards() {
        List<RewardResponse> responses = rewardService.getAvailableRewards();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/me")
    @Operation(
        summary = "내가 받을 수 있는 리워드 조회",
        description = "현재 로그인한 고객의 피드백 수에 따라 받을 수 있는 리워드 목록을 조회합니다. 피드백 수가 부족한 리워드는 제외됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 리워드 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": [{\"id\": 1, \"title\": \"10% 할인\", \"description\": \"다음 방문 시 10% 할인\", \"requiredFeedbackCount\": 5, \"canRedeem\": true}]}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다")
    })
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getMyAvailableRewards(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<RewardResponse> responses = rewardService.getAvailableRewardsForUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/redeem")
    @Operation(
        summary = "리워드 교환",
        description = "현재 로그인한 고객이 리워드를 교환합니다. 피드백 수가 부족하거나 이미 교환한 리워드는 교환할 수 없습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리워드 교환 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RewardRedemptionResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": {\"id\": 1, \"rewardId\": 1, \"rewardTitle\": \"10% 할인\", \"status\": \"REDEEMED\", \"expiresAt\": \"2024-02-01T10:00:00\", \"redeemedAt\": \"2024-01-01T10:00:00\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (피드백 수 부족, 이미 교환한 리워드 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리워드를 찾을 수 없습니다")
    })
    public ResponseEntity<ApiResponse<RewardRedemptionResponse>> redeemReward(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "리워드 교환 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RewardRedemptionRequest.class),
                    examples = @ExampleObject(
                        value = "{\"rewardId\": 1}"
                    )
                )
            )
            @RequestBody RewardRedemptionRequest request) {
        
        RewardRedemptionResponse response = rewardService.redeemReward(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/redemptions/me")
    @Operation(
        summary = "내 리워드 교환 내역 조회",
        description = "현재 로그인한 고객의 모든 리워드 교환 내역을 조회합니다. 사용한 리워드, 만료된 리워드 등 모든 상태를 포함합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 리워드 내역 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": [{\"id\": 1, \"rewardId\": 1, \"rewardTitle\": \"10% 할인\", \"status\": \"USED\", \"redeemedAt\": \"2024-01-01T10:00:00\", \"usedAt\": \"2024-01-15T15:30:00\"}]}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다")
    })
    public ResponseEntity<ApiResponse<List<RewardRedemptionResponse>>> getMyRedemptions(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<RewardRedemptionResponse> responses = rewardService.getUserRedemptions(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/redemptions/me/active")
    @Operation(
        summary = "내가 사용 가능한 리워드 조회",
        description = "현재 로그인한 고객이 교환했지만 아직 사용하지 않은 리워드를 조회합니다. 만료된 리워드는 제외됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용 가능한 리워드 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": [{\"id\": 1, \"rewardId\": 1, \"rewardTitle\": \"10% 할인\", \"status\": \"REDEEMED\", \"expiresAt\": \"2024-02-01T10:00:00\", \"discountRate\": 0.1}]}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다")
    })
    public ResponseEntity<ApiResponse<List<RewardRedemptionResponse>>> getMyActiveRedemptions(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        List<RewardRedemptionResponse> responses = rewardService.getActiveRedemptions(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/redemptions/{redemptionId}/use")
    @Operation(
        summary = "리워드 사용 처리",
        description = "교환된 리워드를 사용 처리합니다. 매장에서 주문 시 리워드를 적용할 때 사용합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리워드 사용 처리 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": null}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 사용된 리워드, 만료된 리워드 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리워드 교환 내역을 찾을 수 없습니다")
    })
    public ResponseEntity<ApiResponse<Void>> markRedemptionAsUsed(
            @Parameter(description = "교환 ID", example = "1")
            @PathVariable("redemptionId") Long redemptionId) {
        
        rewardService.markRedemptionAsUsed(redemptionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/redemptions/{redemptionId}/cancel")
    @Operation(
        summary = "리워드 사용 취소",
        description = "사용된 리워드의 사용을 취소하여 다시 사용 가능하게 만듭니다. 주문 취소 시 리워드를 복구할 때 사용합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리워드 사용 취소 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": null}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (사용되지 않은 리워드, 만료된 리워드 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리워드 교환 내역을 찾을 수 없습니다")
    })
    public ResponseEntity<ApiResponse<Void>> cancelRedemption(
            @Parameter(description = "교환 ID", example = "1")
            @PathVariable("redemptionId") Long redemptionId) {
        
        rewardService.cancelRedemption(redemptionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/eligible")
    @Operation(
        summary = "내 리워드 수령 자격 확인",
        description = "현재 로그인한 고객의 피드백 수에 따른 리워드 수령 자격을 확인합니다. 최소 피드백 수 조건을 충족하는지 확인할 수 있습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "리워드 자격 확인 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": true}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다")
    })
    public ResponseEntity<ApiResponse<Boolean>> checkMyRewardEligibility(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        boolean eligible = rewardService.isEligibleForReward(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(eligible));
    }
}