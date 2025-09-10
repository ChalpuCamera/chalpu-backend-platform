package com.example.chalpuplatform.jar.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NPSLevel {
    CRITICAL("개선 필요", -100, -1),      // 0점 미만
    ACCEPTABLE("양호", 0, 29),            // 0-29점
    GOOD("우수", 30, 49),                 // 30-49점
    EXCELLENT("매우 우수", 50, 69),       // 50-69점
    WORLD_CLASS("최고 수준", 70, 100);    // 70점 이상
    
    private final String koreanName;
    private final int minScore;
    private final int maxScore;
    
    public static NPSLevel fromScore(double score) {
        if (score < 0) {
            return CRITICAL;
        } else if (score < 30) {
            return ACCEPTABLE;
        } else if (score < 50) {
            return GOOD;
        } else if (score < 70) {
            return EXCELLENT;
        } else {
            return WORLD_CLASS;
        }
    }
    
    public String getDescription() {
        return switch (this) {
            case CRITICAL -> "즉각적인 개선이 필요한 수준입니다.";
            case ACCEPTABLE -> "업계 평균 수준으로 지속적인 개선이 필요합니다.";
            case GOOD -> "양호한 고객 만족도를 보이고 있습니다.";
            case EXCELLENT -> "매우 우수한 고객 충성도를 달성했습니다.";
            case WORLD_CLASS -> "세계 최고 수준의 고객 경험을 제공하고 있습니다.";
        };
    }
}