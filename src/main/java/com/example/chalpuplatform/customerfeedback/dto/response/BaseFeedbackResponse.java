package com.example.chalpuplatform.customerfeedback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "피드백 기본 응답")
public class BaseFeedbackResponse {
    @Schema(description = "피드백 ID", example = "1")
    private Long feedbackId;

    @Schema(description = "음식 이름", example = "김치찌개")
    private String foodName;

    @Schema(description = "매장 이름", example = "맛있는집")
    private String storeName;

    @Schema(description = "설문 이름", example = "음식 만족도 조사")
    private String surveyName;

    @Schema(description = "피드백 생성일")
    private LocalDateTime createdAt;

    @Schema(description = "사장님이 피드백을 조회했는지 여부", example = "false")
    private Boolean isViewed;
}
