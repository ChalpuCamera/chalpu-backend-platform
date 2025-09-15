package com.example.chalpuplatform.customerfeedback.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.customerfeedback.service.CustomerFeedbackService;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.chalpuplatform.customerfeedback.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-feedback")
@RequiredArgsConstructor
@Tag(name = "고객 피드백", description = "고객 음식 피드백 관리 API - 음식 리뷰, 사진 업로드, 설문 응답을 통합 관리합니다")
public class CustomerFeedbackController {

    private final CustomerFeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
        summary = "피드백 생성",
        description = "음식에 대한 피드백과 설문 응답을 생성합니다. 사진 업로드를 위한 S3 presigned URL 생성 API를 먼저 호출해야 합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "피드백 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FeedbackResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": {\"id\": 1, \"storeId\": 1, \"foodId\": 1, \"surveyId\": 1, \"photoUrls\": [\"https://s3.../photo1.jpg\"], \"surveyAnswers\": [{\"questionId\": 1, \"answerText\": \"맛있었습니다\", \"numericValue\": 4.5}], \"createdAt\": \"2024-01-01T10:00:00\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장, 음식 또는 설문을 찾을 수 없습니다")
    })
    public ResponseEntity<ApiResponse<FeedbackResponse>> createFeedback(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "피드백 생성 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FeedbackCreateRequest.class),
                    examples = @ExampleObject(
                        value = "{\"storeId\": 1, \"foodId\": 1, \"surveyId\": 1, \"photoS3Keys\": [\"feedback-photos/customer1/uuid1.jpg\"], \"surveyAnswers\": [{\"questionId\": 1, \"answerText\": \"맛있었습니다\", \"numericValue\": 4.5}]}"
                    )
                )
            )
            @RequestBody FeedbackCreateRequest request) {
        
        FeedbackResponse response = feedbackService.createFeedback(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
        summary = "내 피드백 목록 조회",
        description = "현재 로그인한 고객이 작성한 모든 피드백을 페이징으로 조회합니다. 내가 방문한 매장들에 대한 피드백 내역을 확인할 수 있습니다. 기본 20개씩 최신 순으로 정렬됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "내 피드백 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": {\"content\": [{\"id\": 1, \"storeId\": 1, \"storeName\": \"맛집\", \"foodId\": 1, \"foodName\": \"지리산\", \"photoUrls\": [\"https://s3.../photo1.jpg\"], \"createdAt\": \"2024-01-01T10:00:00\"}], \"page\": 0, \"size\": 20, \"totalElements\": 50, \"totalPages\": 3, \"hasNext\": true, \"hasPrevious\": false}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다")
    })
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> getMyFeedbacks(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(
                description = "페이징 정보 (page=0부터 시작, size=페이지 사이즈, sort=정렬 기준)",
                examples = @ExampleObject(
                    name = "기본 예시",
                    value = "page=0&size=20&sort=createdAt,desc"
                )
            )
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FeedbackResponse> responses = feedbackService.getUserFeedbacks(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
    }


    @GetMapping("/store/{storeId}")
    @Operation(
        summary = "사장님이 매장별 피드백(리뷰) 목록 조회하는 API",
        description = "특정 매장에 대한 모든 고객 피드백을 페이징으로 조회합니다. 매장 사장이 고객 반응을 확인할 때 사용합니다. 기본 20개씩 최신 순으로 정렬됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "매장 피드백 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": {\"content\": [{\"id\": 1, \"customerId\": 2, \"customerName\": \"홍길동\", \"foodId\": 1, \"foodName\": \"지리산\", \"photoUrls\": [\"https://s3.../photo1.jpg\"], \"createdAt\": \"2024-01-01T10:00:00\"}], \"page\": 0, \"size\": 20, \"totalElements\": 100, \"totalPages\": 5, \"hasNext\": true, \"hasPrevious\": false}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매장을 찾을 수 없습니다")
    })
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> getStoreFeedbacks(
            @Parameter(description = "매장 ID", example = "1")
            @PathVariable("storeId") Long storeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<FeedbackResponse> responses = feedbackService.getStoreFeedbacks(storeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
    }

    @GetMapping("/food/{foodId}")
    @Operation(
        summary = "사장님이 음식별 피드백(리뷰) 목록 조회하는 API",
        description = "특정 음식에 대한 모든 고객 피드백을 페이징으로 조회합니다. 매장 사장이 특정 음식에 대한 고객 반응을 확인할 때 사용합니다. 기본 20개씩 최신 순으로 정렬됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "음식 피드백 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": {\"content\": [{\"id\": 1, \"customerId\": 2, \"customerName\": \"홍길동\", \"foodId\": 1, \"foodName\": \"지리산\", \"photoUrls\": [\"https://s3.../photo1.jpg\"], \"createdAt\": \"2024-01-01T10:00:00\"}], \"page\": 0, \"size\": 20, \"totalElements\": 100, \"totalPages\": 5, \"hasNext\": true, \"hasPrevious\": false}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "음식을 찾을 수 없습니다")
    })
    public ResponseEntity<ApiResponse<PageResponse<FeedbackResponse>>> getFoodFeedbacks(
            @Parameter(description = "음식 ID", example = "1")
            @PathVariable("foodId") Long foodId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<FeedbackResponse> responses = feedbackService.getFoodFeedbacks(foodId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(responses)));
    }

    @GetMapping("/{feedbackId}")
    @Operation(
        summary = "특정 피드백 상세 조회하는 API",
        description = "특정 피드백의 상세 정보를 조회합니다. 피드백 사진, 설문 응답, 매장 및 음식 정보를 포함합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "피드백 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FeedbackResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": {\"id\": 1, \"storeId\": 1, \"storeName\": \"맛집\", \"foodId\": 1, \"foodName\": \"지리산\", \"photoUrls\": [\"https://s3.../photo1.jpg\"], \"surveyAnswers\": [{\"questionId\": 1, \"questionText\": \"맛은 어때는지요?\", \"answerText\": \"맛있었습니다\", \"numericValue\": 4.5}], \"createdAt\": \"2024-01-01T10:00:00\"}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "피드백을 찾을 수 없습니다")
    })
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedback(
            @Parameter(description = "피드백 ID", example = "1")
            @PathVariable("feedbackId") Long feedbackId) {
        
        FeedbackResponse response = feedbackService.getFeedbackById(feedbackId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/presigned-urls")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
        summary = "피드백 사진들 Presigned URL 생성",
        description = """
            고객이 여러 개의 피드백 사진을 S3에 직접 업로드하기 위한 Presigned URL들을 한 번에 생성합니다.
            
            **클라이언트 처리 순서:**
            1. 이 API를 호출하여 각 파일별 `presignedUrl`과 `s3Key`를 받습니다.
            2. 각 `presignedUrl`에 해당하는 파일을 **HTTP PUT** 요청으로 업로드합니다.
               - **주의:** `Content-Type` 헤더에 반드시 실제 파일의 MIME 타입(예: `image/jpeg`)을 포함해야 합니다.
            3. 모든 S3 업로드가 성공하면, 피드백 생성 API를 호출할 때 모든 `s3Key`를 포함해서 전송합니다.
            
            **사용 예시:**
            - 1개 파일: `["photo1.jpg"]`
            - 여러개 파일: `["photo1.jpg", "photo2.jpg", "photo3.jpg"]`
        """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Presigned URL 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FeedbackPhotosPresignedUrlResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\": true, \"message\": \"성공했습니다\", \"data\": {\"presignedUrls\": [{\"fileName\": \"photo1.jpg\", \"presignedUrl\": \"https://s3.../presigned-url\", \"s3Key\": \"feedback-photos/customer1/uuid1.jpg\"}]}}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (너무 많은 파일 또는 잘못된 파일 확장자)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증이 필요합니다")
    })
    public ResponseEntity<ApiResponse<FeedbackPhotosPresignedUrlResponse>> generateMultiplePresignedUrls(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "피드백 사진 업로드 요청",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = FeedbackPhotosUploadRequest.class),
                    examples = @ExampleObject(
                        value = "{\"fileNames\": [\"photo1.jpg\", \"photo2.jpg\", \"photo3.jpg\"]}"
                    )
                )
            )
            @RequestBody FeedbackPhotosUploadRequest request) {
        
        FeedbackPhotosPresignedUrlResponse response = feedbackService.generateMultipleFeedbackPhotosPresignedUrl(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}