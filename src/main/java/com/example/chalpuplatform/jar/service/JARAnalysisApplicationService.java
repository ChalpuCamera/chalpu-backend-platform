package com.example.chalpuplatform.jar.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.jar.domain.JARAnalysisResult;
import com.example.chalpuplatform.jar.domain.JARAttribute;
import com.example.chalpuplatform.jar.domain.JARDataPoint;
import com.example.chalpuplatform.jar.domain.NPSResult;
import com.example.chalpuplatform.jar.dto.JARAnalysisResponse;
import com.example.chalpuplatform.jar.dto.SingleJARResponse;
import com.example.chalpuplatform.jar.exception.JARException;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import com.example.chalpuplatform.survey.repository.SurveyAnswerRepository;
import com.example.chalpuplatform.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class JARAnalysisApplicationService {
    
    private final JARAnalysisService jarAnalysisService;
    private final NPSCalculationService npsCalculationService;
    private final SurveyAnswerRepository answerRepository;
    private final SurveyQuestionRepository questionRepository;
    private final FoodItemRepository foodItemRepository;
    
    public JARAnalysisResponse analyzeFoodItemJAR(Long foodItemId, LocalDate startDate, LocalDate endDate) {
        // 날짜 검증
        if (startDate.isAfter(endDate)) {
            throw new JARException(ErrorMessage.JAR_INVALID_DATE_RANGE);
        }
        
        // 음식 아이템 존재 여부 확인
        if (!foodItemRepository.existsById(foodItemId)) {
            throw new JARException(ErrorMessage.FOODITEM_NOT_FOUND);
        }
        
        log.info("event=jar_food_analysis_started, food_id={}, start_date={}, end_date={}", foodItemId, startDate, endDate);
        
        // JAR 데이터 조회
        List<Object[]> rawData = answerRepository.findJARDataByFoodItem(
            foodItemId,
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        );
        
        if (rawData.isEmpty()) {
            log.warn("event=jar_food_data_not_found, food_id={}", foodItemId);
            throw new JARException(ErrorMessage.JAR_INSUFFICIENT_DATA);
        }
        
        // 데이터를 질문별로 그룹화하고 분석
        Map<Long, List<Object[]>> groupedByQuestion = rawData.stream()
            .collect(Collectors.groupingBy(row -> (Long) row[0]));
        
        List<JARAnalysisResult> results = groupedByQuestion.entrySet().stream()
            .map(entry -> {
                Long questionId = entry.getKey();
                List<Object[]> questionData = entry.getValue();
                JARAttribute attribute = (JARAttribute) questionData.get(0)[1];
                
                List<JARDataPoint> dataPoints = questionData.stream()
                    .filter(row -> row[2] != null && row[3] != null && row[4] != null)
                    .map(row -> {
                        float jarScore = ((Number) row[2]).floatValue() - 3.0f;
                        float npsRecommend = ((Number) row[3]).floatValue();
                        float npsReorder = ((Number) row[4]).floatValue();
                        float npsWeighted = npsRecommend * 0.6f + npsReorder * 0.4f;
                        return new JARDataPoint(jarScore, npsWeighted);
                    })
                    .toList();
                
                return jarAnalysisService.analyzeJAR(questionId, attribute, dataPoints);
            })
            .sorted((a, b) -> Double.compare(b.getTotalPenalty(), a.getTotalPenalty()))
            .toList();
        
        log.info("event=jar_food_analysis_completed, food_id={}, question_count={}", foodItemId, results.size());
        
        // NPS 데이터 조회 및 계산
        List<Object[]> npsData = answerRepository.findNPSDataByFoodItem(
            foodItemId,
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        );
        
        NPSResult npsResult = npsCalculationService.calculateNPS(npsData);
        log.info("event=nps_analysis_completed, food_id={}, nps_score={}", foodItemId, npsResult.score());
        
        return new JARAnalysisResponse(results, npsResult, LocalDateTime.now());
    }
    
    public SingleJARResponse analyzeQuestion(Long questionId) {
        // 질문 조회 및 JAR 질문 여부 확인
        SurveyQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new JARException(ErrorMessage.SURVEY_QUESTION_NOT_FOUND));
            
        if (!question.isJARQuestion()) {
            log.warn("event=jar_invalid_question, question_id={}", questionId);
            throw new JARException(ErrorMessage.JAR_INVALID_QUESTION);
        }
        
        log.info("event=jar_question_analysis_started, question_id={}", questionId);

        // 해당 질문의 JAR 데이터 조회
        List<Object[]> rawData = answerRepository.findJARDataByQuestion(questionId);

        if (rawData.isEmpty()) {
            log.warn("event=jar_question_data_not_found, question_id={}", questionId);
            throw new JARException(ErrorMessage.JAR_INSUFFICIENT_DATA);
        }

        // 데이터 변환
        List<JARDataPoint> dataPoints = rawData.stream()
            .filter(row -> row[0] != null && row[1] != null && row[2] != null)
            .map(row -> {
                float jarScore = ((Number) row[0]).floatValue() - 3.0f;
                float npsRecommend = ((Number) row[1]).floatValue();
                float npsReorder = ((Number) row[2]).floatValue();
                float npsWeighted = npsRecommend * 0.6f + npsReorder * 0.4f;
                return new JARDataPoint(jarScore, npsWeighted);
            })
            .toList();

        // JAR 분석 실행
        JARAnalysisResult result = jarAnalysisService.analyzeJAR(
            questionId,
            question.getJarAttribute(),
            dataPoints
        );
        
        log.info("event=jar_question_analysis_completed, question_id={}, response_count={}", 
                questionId, result.totalResponses());
        
        return new SingleJARResponse(questionId, result);
    }
}