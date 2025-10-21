package com.example.chalpuplatform.notification.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.notification.dto.CreateNotificationRequest;
import com.example.chalpuplatform.notification.dto.CreateNotificationResponse;
import com.example.chalpuplatform.notification.service.StoreNotificationService;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/owner/notifications")
@RequiredArgsConstructor
@Tag(name = "알림(사장님)", description = "사장님용 구독자 알림 API")
public class StoreNotificationController {

    private final StoreNotificationService notificationService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
        summary = "구독자에게 알림 발송",
        description = "매장 구독자들에게 특가, 이벤트 등의 알림을 발송합니다. 알림은 비동기로 처리되며 즉시 반환됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "알림 생성 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"code\": 200, \"message\": \"API 요청이 성공했습니다.\", \"result\": {\"notificationId\": 1, \"targetSubscriberCount\": 150, \"message\": \"알림 생성 완료: 150명에게 발송 예정\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (구독자 없음, FCM 토큰 없음 등)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "해당 매장에 대한 권한이 없습니다"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "매장을 찾을 수 없습니다"
        )
    })
    public ResponseEntity<ApiResponse<CreateNotificationResponse>> createNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "알림 생성 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateNotificationRequest.class),
                    examples = @ExampleObject(
                        value = "{\"storeId\": 1, \"type\": \"SPECIAL_OFFER\", \"title\": \"오늘의 특가 이벤트\", \"message\": \"2025년 10월 22일 ~메뉴를 특가로 현장 방문 5분께 15000원에 드려요\", \"scheduledAt\": null, \"data\": {\"eventId\": \"123\"}}"
                    )
                )
            )
            @RequestBody CreateNotificationRequest request) {

        CreateNotificationResponse response = notificationService.createNotification(
                userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
