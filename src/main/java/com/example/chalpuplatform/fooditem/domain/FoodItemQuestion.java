package com.example.chalpuplatform.fooditem.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food_item_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_food_question",
                        columnNames = {"food_item_id", "question_id"}
                )
        },
        indexes = {
                @Index(name = "idx_food_item_questions_active", columnList = "food_item_id, is_active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodItemQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public FoodItemQuestion(FoodItem foodItem, SurveyQuestion question, Boolean isActive) {
        this.foodItem = foodItem;
        this.question = question;
        this.isActive = isActive != null ? isActive : true;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean belongsToFoodItem(Long foodItemId) {
        return this.foodItem.getId().equals(foodItemId);
    }
}
