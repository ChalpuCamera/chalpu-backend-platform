package com.example.chalpuplatform.coupon.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.coupon.dto.*;
import com.example.chalpuplatform.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Tag(name = "쿠폰", description = "손님용 쿠폰 스탬프 API")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/membership")
    @Operation(
        summary = "쿠폰 멤버십 조회",
        description = "손님의 현재 스탬프 개수를 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"API 요청이 성공했습니다.\", \"result\": {\"currentStamps\": 5, \"canRedeem\": false}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 전화번호"
        )
    })
    public ResponseEntity<ApiResponse<CouponMembershipResponse>> getMembership(
            @Parameter(description = "매장 ID", example = "1")
            @RequestParam("storeId") Long storeId,
            @Parameter(description = "전화번호", example = "010-1234-5678")
            @RequestParam("phone") String phone) {

        CouponMembershipResponse response = couponService.getMembership(storeId, phone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/earn")
    @Operation(
        summary = "스탬프 적립",
        description = "사장님이 발급한 PIN을 입력하여 스탬프를 적립합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "적립 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"API 요청이 성공했습니다.\", \"result\": {\"success\": true, \"currentStamps\": 7}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (만료된 PIN, 이미 사용된 PIN 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "유효하지 않은 PIN"
        )
    })
    public ResponseEntity<ApiResponse<CouponEarnResponse>> earnStamps(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "스탬프 적립 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CouponEarnRequest.class),
                    examples = @ExampleObject(
                        value = "{\"storeId\": 1, \"phone\": \"010-1234-5678\", \"pin\": \"47\"}"
                    )
                )
            )
            @RequestBody CouponEarnRequest request) {

        CouponEarnResponse response = couponService.earnStamps(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/redeem")
    @Operation(
        summary = "쿠폰 사용",
        description = "스탬프 10개를 사용하여 쿠폰을 사용합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"API 요청이 성공했습니다.\", \"result\": {\"success\": true, \"currentStamps\": 0}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "스탬프 부족"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "멤버십을 찾을 수 없음"
        )
    })
    public ResponseEntity<ApiResponse<CouponRedeemResponse>> redeemCoupon(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "쿠폰 사용 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CouponRedeemRequest.class),
                    examples = @ExampleObject(
                        value = "{\"storeId\": 1, \"phone\": \"010-1234-5678\"}"
                    )
                )
            )
            @RequestBody CouponRedeemRequest request) {

        CouponRedeemResponse response = couponService.redeemCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
