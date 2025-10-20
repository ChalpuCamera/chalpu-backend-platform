package com.example.chalpuplatform.coupon.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.coupon.dto.CouponEarnStampsByOwnerRequest;
import com.example.chalpuplatform.coupon.dto.CouponEarnStampsByOwnerResponse;
import com.example.chalpuplatform.coupon.service.CouponService;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/coupon")
@RequiredArgsConstructor
@Tag(name = "쿠폰(사장님)", description = "사장님용 쿠폰 스탬프 API")
public class CouponOwnerController {

    private final CouponService couponService;

    @PostMapping("/earn-stamps")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "스탬프 적립",
        description = "고객이 생성한 PIN을 입력하고 스탬프 개수를 설정하여 적립합니다. PIN은 3분간 유효합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "스탬프 적립 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"API 요청이 성공했습니다.\", \"result\": {\"success\": true, \"currentStamps\": 7, \"addedStamps\": 2}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (만료된 PIN, 이미 사용된 PIN 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증이 필요합니다"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "해당 매장에 대한 권한이 없습니다"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "유효하지 않은 PIN"
        )
    })
    public ResponseEntity<ApiResponse<CouponEarnStampsByOwnerResponse>> earnStamps(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "스탬프 적립 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CouponEarnStampsByOwnerRequest.class),
                    examples = @ExampleObject(
                        value = "{\"storeId\": 1, \"pin\": \"47\", \"stamps\": 2}"
                    )
                )
            )
            @RequestBody CouponEarnStampsByOwnerRequest request) {

        CouponEarnStampsByOwnerResponse response = couponService.earnStampsByOwner(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
