package com.example.chalpuplatform.jar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record JARAnalysisRequest(
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,
    
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate
) {}