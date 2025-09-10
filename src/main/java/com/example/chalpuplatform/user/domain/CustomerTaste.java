package com.example.chalpuplatform.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CustomerTaste {

    @Column(name = "spicy_level")
    private Integer spicyLevel;  // 매운맛 정도 (1-5)

    @Column(name = "meal_amount")
    private Integer mealAmount;  // 식사량 (1-5)

    @Column(name = "meal_spending")
    private Integer mealSpending;  // 식사 지출 정도 (1-5)

    public void validateTasteValues() {
        if (spicyLevel != null && (spicyLevel < 1 || spicyLevel > 5)) {
            throw new IllegalArgumentException("매운맛 정도는 1-5 범위여야 합니다.");
        }
        if (mealAmount != null && (mealAmount < 1 || mealAmount > 5)) {
            throw new IllegalArgumentException("식사량은 1-5 범위여야 합니다.");
        }
        if (mealSpending != null && (mealSpending < 1 || mealSpending > 5)) {
            throw new IllegalArgumentException("식사 지출 정도는 1-5 범위여야 합니다.");
        }
    }
}
