package com.example.chalpuplatform.customerfeedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "음식별 읽지 않은 피드백 개수 응답")
public class FeedbackUnreadCountResponse {

    @Schema(description = "음식 ID", example = "1")
    private Long foodItemId;

    @Schema(description = "음식 이름", example = "김치찌개")
    private String foodName;

    @Schema(description = "읽지 않은 피드백 개수", example = "3")
    private Long unreadCount;

    @Schema(description = "전체 피드백 개수", example = "10")
    private Long totalCount;
}