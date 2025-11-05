package com.example.chalpuplatform.customerfeedback.dto.response;

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
@Schema(description = "사장님용 피드백 상세 응답 (상세 조회용)")
public class OwnerFeedbackDetailResponse extends BaseFeedbackResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "음식러버")
    private String userNickname;

    @Schema(description = "고객 매운맛 선호도 (1-5)", example = "3")
    private Integer spicyLevel;

    @Schema(description = "고객 식사량 (1-5)", example = "4")
    private Integer mealAmount;

    @Schema(description = "고객 식사 지출 정도 (1-5)", example = "3")
    private Integer mealSpending;

    @Schema(description = "설문 답변 목록")
    private List<SurveyAnswerResponse> surveyAnswers;

    @Schema(description = "피드백 사진 목록")
    private List<String> photoUrls;
}
