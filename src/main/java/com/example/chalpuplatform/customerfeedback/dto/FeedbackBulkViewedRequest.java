package com.example.chalpuplatform.customerfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "다건 피드백 읽음 처리 요청")
public class FeedbackBulkViewedRequest {

    @NotEmpty(message = "피드백 ID 목록은 비어있을 수 없습니다")
    @Size(max = 100, message = "한 번에 최대 100개까지 처리 가능합니다")
    @Schema(description = "읽음 처리할 피드백 ID 목록", example = "[1, 2, 3, 4, 5]")
    private List<Long> feedbackIds;
}