package com.example.chalpuplatform.user.dto;

import com.example.chalpuplatform.user.domain.CustomerTaste;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerTasteDto {
    
    private Integer spicyLevel;
    private Integer mealAmount;
    private Integer mealSpending;
    
    public static CustomerTasteDto  from(CustomerTaste customerTaste) {
        if (customerTaste == null) {
            return null;
        }
        
        return CustomerTasteDto.builder()
                .spicyLevel(customerTaste.getSpicyLevel())
                .mealAmount(customerTaste.getMealAmount())
                .mealSpending(customerTaste.getMealSpending())
                .build();
    }
    
    public CustomerTaste toEntity() {
        return CustomerTaste.builder()
                .spicyLevel(this.spicyLevel)
                .mealAmount(this.mealAmount)
                .mealSpending(this.mealSpending)
                .build();
    }
    
    public void validate() {
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