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
@Schema(description = "사진 업로드를 위한 Presigned URL 요청")
public class PhotoUploadRequest {

    @Schema(description = "업로드할 파일의 원본 이름", example = "kimchi-stew.jpg")
    private String fileName;
}