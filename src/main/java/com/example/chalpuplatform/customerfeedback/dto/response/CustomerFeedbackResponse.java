package com.example.chalpuplatform.customerfeedback.dto.response;

import com.example.chalpuplatform.customerfeedback.domain.CustomerFeedback;
import com.example.chalpuplatform.customerfeedback.dto.SurveyAnswerResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "고객용 피드백 응답 (본인 피드백만)")
public class CustomerFeedbackResponse extends BaseFeedbackResponse {
    @Schema(description = "캠페인 id", example = "1")
    private Long campaignId;

    @Schema(description = "설문 답변 목록")
    private List<SurveyAnswerResponse> surveyAnswers;

    @Schema(description = "피드백 사진 목록")
    private List<String> photoUrls;

    public static CustomerFeedbackResponse from(CustomerFeedback feedback) {
        return CustomerFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .foodName(feedback.getFoodItem().getFoodName())
                .storeName(feedback.getStore().getStoreName())
                .surveyName(feedback.getSurvey().getSurveyName())
                .createdAt(feedback.getCreatedAt())
                .isViewed(feedback.getIsViewed())
                .campaignId(feedback.getCampaign() != null ? feedback.getCampaign().getId() : null)
                .build();
    }
}
