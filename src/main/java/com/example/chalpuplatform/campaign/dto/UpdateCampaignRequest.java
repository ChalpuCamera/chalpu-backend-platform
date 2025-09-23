package com.example.chalpuplatform.campaign.dto;

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
public class UpdateCampaignRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer targetFeedbackCount;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;
}