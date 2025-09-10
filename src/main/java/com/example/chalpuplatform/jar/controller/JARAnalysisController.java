package com.example.chalpuplatform.jar.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.jar.dto.*;
import com.example.chalpuplatform.jar.service.JARAnalysisApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/jar")
@RequiredArgsConstructor
@Slf4j
public class JARAnalysisController {
    
    private final JARAnalysisApplicationService jarApplicationService;
    
    /**
     * 단일 질문 JAR 분석
     */
    @GetMapping("/questions/{questionId}/analysis")
    public ApiResponse<SingleJARResponse> analyzeQuestion(
            @PathVariable("questionId") Long questionId) {
        log.info("event=jar_analysis_requested, question_id={}", questionId);
        SingleJARResponse response = jarApplicationService.analyzeQuestion(questionId);
        return ApiResponse.success(response);
    }
    

    /**
     * 특정 음식 JAR 분석
     */
    @GetMapping("/foods/{foodId}/analysis")
    public ApiResponse<JARAnalysisResponse> analyzeFoodItem(
            @PathVariable("foodId") Long foodId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        // 기본값 설정: startDate가 없으면 30일 전, endDate가 없으면 오늘
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        log.info("event=jar_food_analysis_requested, food_id={}, start_date={}, end_date={}", 
                foodId, startDate, endDate);
        JARAnalysisResponse response = jarApplicationService.analyzeFoodItemJAR(
            foodId,
            startDate,
            endDate
        );
        return ApiResponse.success(response);
    }
}