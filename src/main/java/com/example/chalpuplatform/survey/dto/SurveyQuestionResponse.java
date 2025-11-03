package com.example.chalpuplatform.survey.dto;

import com.example.chalpuplatform.jar.domain.JARAttribute;
import com.example.chalpuplatform.survey.domain.QuestionType;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "서베이 질문 응답")
public class SurveyQuestionResponse {

    @Schema(description = "질문 ID", example = "1")
    private Long questionId;

    @Schema(description = "질문 텍스트", example = "오늘 드신 음식의 맵기는 어떠셨나요?")
    private String questionText;

    @Schema(description = "JAR 속성", example = "SPICINESS")
    private JARAttribute jarAttribute;

    @Schema(description = "질문 타입", example = "SLIDER")
    private QuestionType questionType;

    public static SurveyQuestionResponse from(SurveyQuestion question) {
        return SurveyQuestionResponse.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .jarAttribute(question.getJarAttribute())
                .questionType(question.getQuestionType())
                .build();
    }
}
