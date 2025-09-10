package com.example.chalpuplatform.jar.service;

import com.example.chalpuplatform.jar.domain.NPSCategory;
import com.example.chalpuplatform.jar.domain.NPSResult;
import com.example.chalpuplatform.survey.domain.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NPSCalculationService {
    
    // 가중치 설정
    private static final double RECOMMEND_WEIGHT = 0.6;  // 추천 의향 60%
    private static final double REORDER_WEIGHT = 0.4;    // 재주문 의향 40%
    
    /**
     * NPS 점수 계산
     * @param npsData [feedbackId, questionType, numericValue] 형태의 데이터
     * @return NPS 결과
     */
    public NPSResult calculateNPS(List<Object[]> npsData) {
        if (npsData == null || npsData.isEmpty()) {
            log.info("event=nps_no_data");
            return NPSResult.empty();
        }
        
        // feedback별로 그룹화
        Map<Long, Map<QuestionType, Float>> feedbackScores = npsData.stream()
            .collect(Collectors.groupingBy(
                row -> (Long) row[0],  // feedbackId
                Collectors.toMap(
                    row -> (QuestionType) row[1],  // questionType
                    row -> (Float) row[2],          // score
                    (v1, v2) -> v2  // 중복 시 마지막 값 사용
                )
            ));
        
        int promoters = 0;
        int passives = 0;
        int detractors = 0;
        int totalResponses = 0;
        
        // 각 피드백별 NPS 카테고리 분류
        for (Map.Entry<Long, Map<QuestionType, Float>> entry : feedbackScores.entrySet()) {
            Map<QuestionType, Float> scores = entry.getValue();
            
            Float recommendScore = scores.get(QuestionType.NPS_RECOMMEND);
            Float reorderScore = scores.get(QuestionType.NPS_REORDER);
            
            // 두 점수가 모두 있는 경우만 계산
            if (recommendScore != null && reorderScore != null) {
                // 가중평균 계산
                double weightedScore = (recommendScore * RECOMMEND_WEIGHT) + (reorderScore * REORDER_WEIGHT);
                
                // 카테고리 분류 (한국형 기준)
                NPSCategory category = NPSCategory.fromScore(weightedScore);
                
                switch (category) {
                    case PROMOTER -> promoters++;
                    case PASSIVE -> passives++;
                    case DETRACTOR -> detractors++;
                }
                totalResponses++;
            }
        }
        
        if (totalResponses == 0) {
            log.warn("event=nps_incomplete_responses");
            return NPSResult.empty();
        }
        
        // 비율 계산
        double promoterRate = Math.round((promoters * 1000.0) / totalResponses) / 10.0;  // 소수점 첫째자리
        double passiveRate = Math.round((passives * 1000.0) / totalResponses) / 10.0;
        double detractorRate = Math.round((detractors * 1000.0) / totalResponses) / 10.0;
        
        // NPS 점수 계산 (추천자 비율 - 비추천자 비율)
        double npsScore = Math.round((promoterRate - detractorRate) * 10.0) / 10.0;
        
        log.info("event=nps_calculated, score={}, promoters={}, passives={}, detractors={}, total={}", 
                npsScore, promoters, passives, detractors, totalResponses);
        
        return NPSResult.of(
            npsScore,
            promoterRate,
            passiveRate,
            detractorRate,
            totalResponses
        );
    }
}