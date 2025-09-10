package com.example.chalpuplatform.jar.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NPSCategory {
    PROMOTER("추천자", 8.0, 10.0),      // 8.0 이상
    PASSIVE("중립자", 6.0, 7.9),        // 6.0-7.9  
    DETRACTOR("비추천자", 0.0, 5.9);    // 5.9 이하
    
    private final String koreanName;
    private final double minScore;
    private final double maxScore;
    
    public static NPSCategory fromScore(double score) {
        if (score >= 8.0) {
            return PROMOTER;
        } else if (score >= 6.0) {
            return PASSIVE;
        } else {
            return DETRACTOR;
        }
    }
}