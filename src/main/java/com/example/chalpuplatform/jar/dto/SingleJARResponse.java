package com.example.chalpuplatform.jar.dto;

import com.example.chalpuplatform.jar.domain.JARAnalysisResult;

public record SingleJARResponse(
    Long questionId,
    JARAnalysisResult analysis
) {}