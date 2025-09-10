package com.example.chalpuplatform.photo.dto;

import com.example.chalpuplatform.photo.domain.Photo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사진 응답")
public class PhotoResponse {
    
    @Schema(description = "사진 ID", example = "1")
    private Long photoId;
    
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "음식 ID", example = "1")
    private Long foodItemId;
    
    @Schema(description = "CloudFront를 통해 접근 가능한 이미지 전체 URL", example = "https://cdn.chalpu.com/photos/stores/1/image.jpg")
    private String imageUrl;
    
    @Schema(description = "원본 파일명", example = "image.jpg")
    private String fileName;
    
    @Schema(description = "파일 크기 (bytes)", example = "1024")
    private Integer fileSize;
    
    @Schema(description = "이미지 너비 (px)", example = "1920")
    private Integer imageWidth;
    
    @Schema(description = "이미지 높이 (px)", example = "1080")
    private Integer imageHeight;
    
    @Schema(description = "생성 시간", example = "2024-01-15T09:30:00")
    private LocalDateTime createdAt;
    
    public static PhotoResponse from(Photo photo, String cloudfrontDomain) {
        return PhotoResponse.builder()
                .photoId(photo.getId())
                .foodItemId(photo.getFoodItem() != null ? photo.getFoodItem().getId() : null)
                .imageUrl(buildFullUrl(cloudfrontDomain, photo.getS3Key()))
                .fileName(photo.getFileName())
                .fileSize(photo.getFileSize())
                .imageWidth(photo.getImageWidth())
                .imageHeight(photo.getImageHeight())
                .createdAt(photo.getCreatedAt())
                .build();
    }
    
    private static String buildFullUrl(String cloudfrontDomain, String s3Key) {
        if (cloudfrontDomain == null || s3Key == null) {
            return null;
        }
        return cloudfrontDomain.endsWith("/") ? cloudfrontDomain + s3Key : cloudfrontDomain + "/" + s3Key;
    }
} 