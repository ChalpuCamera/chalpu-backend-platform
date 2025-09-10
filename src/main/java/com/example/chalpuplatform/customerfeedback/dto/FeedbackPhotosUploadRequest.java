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
@Schema(description = "피드백 사진들 업로드를 위한 Presigned URL 요청")
public class FeedbackPhotosUploadRequest {

    @Schema(description = "업로드할 파일들의 원본 이름 목록", 
            example = "[\"feedback-food-1.jpg\", \"feedback-food-2.jpg\", \"feedback-food-3.jpg\"]")
    private List<String> fileNames;
}
