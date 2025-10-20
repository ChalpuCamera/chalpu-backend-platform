package com.example.chalpuplatform.coupon.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.coupon.dto.CouponIssuePinRequest;
import com.example.chalpuplatform.coupon.dto.CouponIssuePinResponse;
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

    @PostMapping("/issue-pin")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "PIN 발급",
        description = "고객에게 지급할 스탬프 개수를 설정하고 PIN을 발급합니다. 발급된 PIN은 3분간 유효합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "PIN 발급 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"API 요청이 성공했습니다.\", \"result\": {\"pin\": \"47\", \"stamps\": 2, \"expiredAt\": \"2024-01-01T10:03:00\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "인증이 필요합니다"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "해당 매장에 대한 권한이 없습니다"
        )
    })
    public ResponseEntity<ApiResponse<CouponIssuePinResponse>> issuePin(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "PIN 발급 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CouponIssuePinRequest.class),
                    examples = @ExampleObject(
                        value = "{\"storeId\": 1, \"stamps\": 2}"
                    )
                )
            )
            @RequestBody CouponIssuePinRequest request) {

        CouponIssuePinResponse response = couponService.issuePin(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
