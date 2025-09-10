package com.example.chalpuplatform.jar.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record NPSResult(
    double score,           // NPS 점수 (-100 ~ 100)
    double promoterRate,    // 추천자 비율 (%)
    double passiveRate,     // 중립자 비율 (%)
    double detractorRate,   // 비추천자 비율 (%)
    int totalResponses,     // 전체 응답 수
    NPSLevel level,         // NPS 수준
    String levelDescription // 수준 설명
) {
    public static NPSResult empty() {
        return new NPSResult(0.0, 0.0, 0.0, 0.0, 0, NPSLevel.ACCEPTABLE, "데이터가 없습니다.");
    }
    
    public static NPSResult of(double score, double promoterRate, double passiveRate, 
                               double detractorRate, int totalResponses) {
        NPSLevel level = NPSLevel.fromScore(score);
        return new NPSResult(
            score, 
            promoterRate, 
            passiveRate, 
            detractorRate, 
            totalResponses,
            level,
            level.getDescription()
        );
    }
    
    public boolean hasData() {
        return totalResponses > 0;
    }
    @JsonIgnore
    public String getLevelName() {
        return level.getKoreanName();
    }
}