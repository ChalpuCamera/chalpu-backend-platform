package com.example.chalpuplatform.customerfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "설문 답변 요청")
public class SurveyAnswerRequest {

    @Schema(description = "질문 ID", example = "1")
    private Long questionId;

    @Schema(description = "텍스트 답변 (TEXT 타입 질문용)", example = "음식이 매우 맛있었습니다")
    private String answerText;

    @Schema(description = "숫자 응답 (SLIDER: -2.0~2.0, RATING: 0~10)", example = "1.5")
    private Float numericValue;
}