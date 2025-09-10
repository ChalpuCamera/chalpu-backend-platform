package com.example.chalpuplatform.survey.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionType {
    SLIDER("슬라이더", -2.0f, 2.0f),
    TEXT("주관식", null, null),
    RATING("평점", 0f, 10f),
    NPS_RECOMMEND("추천 의향", 0f, 10f),
    NPS_REORDER("재주문 의향", 0f, 10f);

    private final String description;
    private final Float minValue;
    private final Float maxValue;

    public boolean isNumericType() {
        return this == SLIDER || this == RATING || this == NPS_RECOMMEND || this == NPS_REORDER;
    }

    public boolean isTextType() {
        return this == TEXT;
    }
    
    public boolean isNPSType() {
        return this == NPS_RECOMMEND || this == NPS_REORDER;
    }

    public boolean isValidNumericValue(Float value) {
        if (!isNumericType() || value == null) {
            return false;
        }
        return value >= minValue && value <= maxValue;
    }
}