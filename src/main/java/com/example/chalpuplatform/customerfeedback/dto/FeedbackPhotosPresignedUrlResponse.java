package com.example.chalpuplatform.customerfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "피드백 사진들 Presigned URL 발급 응답")
public class FeedbackPhotosPresignedUrlResponse {

    @Schema(description = "각 파일별 presigned URL과 S3 키 정보")
    private List<FeedbackPhotoUrlInfo> photoUrls;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "개별 사진 업로드 정보")
    public static class FeedbackPhotoUrlInfo {
        
        @Schema(description = "원본 파일명", example = "feedback-food-1.jpg")
        private String originalFileName;
        
        @Schema(description = "S3에 업로드할 때 사용할, 유효기간이 설정된 URL")
        private String presignedUrl;
        
        @Schema(description = "업로드 후 S3에 저장될 파일의 고유 키. 피드백 생성 API 호출 시 필요.", 
                example = "feedback-photos/a1b2c3d4-e5f6-7890-1234-567890abcdef.jpg")
        private String s3Key;
    }
}
