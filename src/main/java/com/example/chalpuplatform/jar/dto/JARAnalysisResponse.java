package com.example.chalpuplatform.jar.dto;

import com.example.chalpuplatform.jar.domain.JARAnalysisResult;
import com.example.chalpuplatform.jar.domain.NPSResult;

import java.time.LocalDateTime;
import java.util.List;

public record JARAnalysisResponse(
    List<JARAnalysisResult> results,
    NPSResult npsScore,
    LocalDateTime analyzedAt
) {}