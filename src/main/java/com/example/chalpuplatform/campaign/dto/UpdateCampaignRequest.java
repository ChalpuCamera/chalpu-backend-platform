package com.example.chalpuplatform.campaign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "캠페인 수정 요청")
public class UpdateCampaignRequest {

    @NotNull
    @Schema(description = "캠페인 ID", example = "1", required = true)
    private Long campaignId;

    @NotBlank
    @Size(min = 1, max = 100)
    @Schema(description = "캠페인 이름", example = "업데이트된 캠페인", required = true)
    private String name;

    @Size(max = 500)
    @Schema(description = "캠페인 설명", example = "업데이트된 캠페인 설명")
    private String description;

    @NotNull
    @Min(1)
    @Max(100)
    @Schema(description = "목표 피드백 수", example = "50", required = true)
    private Integer targetFeedbackCount;

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @NotNull
    @Schema(description = "캠페인 시작일", example = "2024-01-01T00:00:00", required = true)
    private LocalDateTime startDate;

    @NotNull
    @Schema(description = "캠페인 종료일", example = "2024-12-31T23:59:59", required = true)
    private LocalDateTime endDate;
}