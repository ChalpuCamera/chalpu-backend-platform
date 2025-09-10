package com.example.chalpuplatform.customerfeedback.dto;

import com.example.chalpuplatform.customerfeedback.domain.CustomerFeedback;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "피드백 응답")
public class FeedbackResponse {

    @Schema(description = "피드백 ID", example = "1")
    private Long id;

    @Schema(description = "음식 이름", example = "김치찌개")
    private String foodName;

    @Schema(description = "매장 이름", example = "맛있는집")
    private String storeName;

    @Schema(description = "사용자 닉네임", example = "음식러버")
    private String userNickname;

    @Schema(description = "설문 이름", example = "음식 만족도 조사")
    private String surveyName;

    @Schema(description = "피드백 생성일")
    private LocalDateTime createdAt;

    @Schema(description = "설문 답변 목록")
    private List<SurveyAnswerResponse> surveyAnswers;

    @Schema(description = "피드백 사진 목록")
    private List<String> photoUrls;

    public static FeedbackResponse from(CustomerFeedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .foodName(feedback.getFoodItem().getFoodName())
                .storeName(feedback.getStore().getStoreName())
                .userNickname(feedback.getUser().getNickname())
                .surveyName(feedback.getSurvey().getSurveyName())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}