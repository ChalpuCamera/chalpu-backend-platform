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
@Schema(description = "사장님용 피드백 응답 (고객 정보 포함)")
public class OwnerFeedbackResponse {

    @Schema(description = "피드백 ID", example = "1")
    private Long feedbackId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

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

    @Schema(description = "사장님께 한마디 (9번 질문 답변)", example = "충분히 맛있었습니다. 감자튀김이 약간 눅눅한 감이 있어서...")
    private String ownerMessage;

    @Schema(description = "피드백 사진 목록")
    private List<String> photoUrls;

    @Schema(description = "사장님이 피드백을 조회했는지 여부", example = "false")
    private Boolean isViewed;

    @Schema(description = "고객 매운맛 선호도 (피드백 당시, 1-5)", example = "3")
    private Integer spicyLevel;

    @Schema(description = "고객 식사량 (피드백 당시, 1-5)", example = "4")
    private Integer mealAmount;

    @Schema(description = "고객 식사 지출 정도 (피드백 당시, 1-5)", example = "3")
    private Integer mealSpending;

    public static OwnerFeedbackResponse from(CustomerFeedback feedback) {
        return OwnerFeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .userId(feedback.getUser().getId())
                .foodName(feedback.getFoodItem().getFoodName())
                .storeName(feedback.getStore().getStoreName())
                .userNickname(feedback.getUser().getNickname())
                .surveyName(feedback.getSurvey().getSurveyName())
                .createdAt(feedback.getCreatedAt())
                .isViewed(feedback.getIsViewed())
                // 스냅샷 값 사용 (피드백 작성 당시의 입맛)
                .spicyLevel(feedback.getSpicyLevelSnapshot())
                .mealAmount(feedback.getMealAmountSnapshot())
                .mealSpending(feedback.getMealSpendingSnapshot())
                .build();
    }
}