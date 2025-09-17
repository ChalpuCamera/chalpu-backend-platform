package com.example.chalpuplatform.customerfeedback.dto;

import com.example.chalpuplatform.survey.domain.QuestionType;
import com.example.chalpuplatform.survey.domain.SurveyAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "설문 답변 응답")
public class SurveyAnswerResponse {

    @Schema(description = "답변 ID", example = "1")
    private Long id;

    @Schema(description = "질문 ID", example = "1")
    private Long questionId;

    @Schema(description = "질문 내용", example = "음식의 맛은 어떠셨나요?")
    private String questionText;

    @Schema(description = "질문 타입", example = "SLIDER")
    private QuestionType questionType;

    @Schema(description = "텍스트 답변 (TEXT 타입용)", example = "매우 맛있었습니다")
    private String answerText;

    @Schema(description = "숫자 응답 (SLIDER/RATING 타입용)", example = "1.5")
    private Float numericValue;

    public static SurveyAnswerResponse from(SurveyAnswer answer) {
        return SurveyAnswerResponse.builder()
                .id(answer.getId())
                .questionId(answer.getQuestion().getId())
                .questionText(answer.getQuestion().getQuestionText())
                .questionType(answer.getQuestion().getQuestionType())
                .answerText(answer.getAnswerText())
                .numericValue(answer.getNumericValue())
                .build();
    }
}