package com.example.chalpuplatform.photo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Presigned URL 발급 응답")
public class PhotoPresignedUrlResponse {

    @Schema(description = "S3에 업로드할 때 사용할, 유효기간이 설정된 URL")
    private String presignedUrl;

    @Schema(description = "업로드 후 S3에 저장될 파일의 고유 키. 업로드 완료 API 호출 시 필요.", example = "photos/stores/1/a1b2c3d4-e5f6-7890-1234-567890abcdef.jpg")
    private String s3Key;
} 