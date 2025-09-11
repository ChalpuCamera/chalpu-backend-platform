package com.example.chalpuplatform.photo.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.photo.dto.*;
import com.example.chalpuplatform.photo.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Tag(name = "사진 API", description = "사진 관련 API")
public class PhotoController {

    private final PhotoService photoService;

    @Operation(summary = "Presigned URL 생성", description = """
            클라이언트가 AWS S3에 파일을 직접 업로드하기 위해 사용하는 Presigned URL을 생성합니다.
            
            **클라이언트 처리 순서:**
            1. 이 API를 호출하여 `presignedUrl`과 `s3Key`를 받습니다.
            2. 받은 `presignedUrl`을 목적지로, 업로드할 파일의 원본 데이터를 body에 담아 **HTTP PUT** 요청을 보냅니다.
               - **주의:** `Content-Type` 헤더에 반드시 실제 파일의 MIME 타입(예: `image/jpeg`)을 포함해야 합니다.
            3. S3 업로드가 성공(HTTP 200 OK)하면, `/api/photos/register` API를 호출하여 업로드 완료 사실을 서버에 알립니다.
               - 이때 응답으로 받았던 `s3Key`와 파일의 원본 이름 등 필요한 메타데이터를 함께 전송합니다.
            """)
    @PostMapping("/presigned-url")
    public ApiResponse<PhotoPresignedUrlResponse> generatePresignedUrl(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PhotoUploadRequest request) {
        Long userId = userDetails.getId();
        return ApiResponse.success(photoService.generatePresignedUrl(userId, request));
    }

    @Operation(summary = "임시 폴더 Presigned URL 생성", description = "tmp 폴더에 업로드하기 위한 Presigned URL을 생성합니다.")
    @PostMapping("/tmp/presigned-url")
    public ApiResponse<PhotoPresignedUrlResponse> generateTmpPresignedUrl(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PhotoUploadRequest request) {
        Long userId = userDetails.getId();
        return ApiResponse.success(photoService.generateTmpPresignedUrl(userId, request));
    }

    @Operation(summary = "사진 정보 등록", description = "S3에 업로드 완료 후, 파일 메타데이터를 서버에 등록합니다.")
    @PostMapping("/register")
    public ApiResponse<PhotoResponse> registerPhoto(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PhotoRegisterRequest request) {
        Long userId = userDetails.getId();
        return ApiResponse.success(photoService.registerPhoto(userId, request));
    }
    
    @Operation(summary = "가게별 사진 목록 조회", description = "특정 가게에 속한 사진 목록을 페이지네이션하여 조회합니다.")
    @GetMapping("/store/{storeId}")
    public ApiResponse<PageResponse<PhotoResponse>> getPhotosByStore(
            @PathVariable Long storeId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(photoService.getPhotosByStore(storeId, pageable));
    }

    @Operation(summary = "음식별 사진 목록 조회", description = "특정 음식에 속한 사진 목록을 페이지네이션하여 조회합니다.")
    @GetMapping("/food-item/{foodItemId}")
    public ApiResponse<PageResponse<PhotoResponse>> getPhotosByFoodItem(
            @PathVariable Long foodItemId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(photoService.getPhotosByFoodItem(foodItemId, pageable));
    }
    
    @Operation(summary = "사진 상세 조회", description = "특정 사진의 상세 정보를 조회합니다.")
    @GetMapping("/{photoId}")
    public ApiResponse<PhotoResponse> getPhoto(@PathVariable Long photoId) {
        return ApiResponse.success(photoService.getPhoto(photoId));
    }

    @Operation(summary = "사진 삭제", description = "특정 사진을 삭제합니다.")
    @DeleteMapping("/{photoId}")
    public ApiResponse<Void> deletePhoto(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long photoId) {
        Long userId = userDetails.getId();
        photoService.deletePhoto(userId, photoId);
        return ApiResponse.success();
    }
} 