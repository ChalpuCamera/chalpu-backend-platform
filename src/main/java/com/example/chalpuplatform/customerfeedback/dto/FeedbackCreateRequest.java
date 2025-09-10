package com.example.chalpuplatform.customerfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "피드백 생성 요청")
public class FeedbackCreateRequest {

    @Schema(description = "음식 ID", example = "1")
    private Long foodId;

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "설문 ID", example = "1")
    private Long surveyId;

    @Schema(description = "설문 답변 목록")
    private List<SurveyAnswerRequest> surveyAnswers;

    @Schema(description = "피드백 사진 목록")
    private List<String> photoS3Keys;
}