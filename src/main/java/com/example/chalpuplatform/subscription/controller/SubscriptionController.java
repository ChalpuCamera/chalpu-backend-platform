package com.example.chalpuplatform.subscription.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.subscription.dto.SubscribeRequest;
import com.example.chalpuplatform.subscription.dto.SubscriptionResponse;
import com.example.chalpuplatform.subscription.service.StoreSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "구독", description = "매장 구독 관리 API")
public class SubscriptionController {

    private final StoreSubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('OWNER')")
    @Operation(summary = "매장 구독", description = "매장을 구독하여 알림을 받습니다.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody SubscribeRequest request) {

        SubscriptionResponse response = subscriptionService.subscribe(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('OWNER')")
    @Operation(summary = "구독 취소", description = "매장 구독을 취소합니다.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> unsubscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("storeId") Long storeId) {

        SubscriptionResponse response = subscriptionService.unsubscribe(userDetails.getId(), storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{storeId}/notification")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('OWNER')")
    @Operation(summary = "알림 설정 토글", description = "구독 중인 매장의 알림을 켜거나 끕니다.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> toggleNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("storeId") Long storeId) {

        SubscriptionResponse response = subscriptionService.toggleNotification(userDetails.getId(), storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('OWNER')")
    @Operation(summary = "구독 정보 조회", description = "특정 매장의 구독 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("storeId") Long storeId) {

        SubscriptionResponse response = subscriptionService.getSubscription(userDetails.getId(), storeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{storeId}/status")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('OWNER')")
    @Operation(summary = "구독 여부 확인", description = "특정 매장의 구독 여부를 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> checkSubscriptionStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable("storeId") Long storeId) {

        boolean isSubscribed = subscriptionService.isSubscribed(userDetails.getId(), storeId);
        return ResponseEntity.ok(ApiResponse.success(isSubscribed));
    }
}
