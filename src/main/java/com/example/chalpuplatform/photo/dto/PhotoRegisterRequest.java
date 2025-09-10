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
@Schema(description = "S3 업로드 완료 후 사진 정보 등록 요청")
public class PhotoRegisterRequest {

    @Schema(description = "Presigned URL 발급 시 받았던 S3 파일 키", example = "platform/a1b2c3d4-e5f6-7890-1234-567890abcdef.jpg")
    private String s3Key;

    @Schema(description = "업로드한 파일의 원본 이름", example = "kimchi-stew.jpg")
    private String fileName;

    @Schema(description = "사진이 속한 음식 아이템의 ID (선택)", example = "10")
    private Long foodItemId;
    
    // 필요에 따라 파일 사이즈, 가로/세로 길이 등 추가 메타데이터 포함 가능
    @Schema(description = "파일 크기 (bytes)", example = "3145728")
    private Integer fileSize;
    
    @Schema(description = "이미지 가로 길이 (px)", example = "1920")
    private Integer imageWidth;

    @Schema(description = "이미지 세로 길이 (px)", example = "1080")
    private Integer imageHeight;
} 