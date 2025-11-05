package com.example.chalpuplatform.customerfeedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "사장님용 피드백 요약 응답 (목록용)")
public class OwnerFeedbackSummaryResponse extends BaseFeedbackResponse {
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
}
