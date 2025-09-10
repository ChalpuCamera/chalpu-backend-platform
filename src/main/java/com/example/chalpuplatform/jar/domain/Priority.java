package com.example.chalpuplatform.jar.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Priority {
    URGENT("긴급"),     // penalty >= 0.4
    HIGH("높음"),       // penalty >= 0.3
    MEDIUM("중간"),     // penalty >= 0.2
    LOW("낮음"),        // penalty >= 0.1
    NORMAL("보통");     // penalty < 0.1
    
    private final String koreanName;
    
    public static Priority fromPenalty(double penalty) {
        if (penalty >= 0.4) {
            return URGENT;
        } else if (penalty >= 0.3) {
            return HIGH;
        } else if (penalty >= 0.2) {
            return MEDIUM;
        } else if (penalty >= 0.1) {
            return LOW;
        } else {
            return NORMAL;
        }
    }
}