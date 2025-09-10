package com.example.chalpuplatform.customerfeedback.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.survey.domain.Survey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CustomerFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem foodItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.SUBMITTED;
    
    @Column(name = "overall_satisfaction")
    private Float overallSatisfaction;
    
    public enum FeedbackStatus {
        SUBMITTED("제출"),
        APPROVED("승인"),
        PENDING("대기"),
        REJECTED("반려");
        
        private final String korean;
        
        FeedbackStatus(String korean) {
            this.korean = korean;
        }
        
        public String getKorean() {
            return korean;
        }
    }

    public static CustomerFeedback createFeedback(FoodItem foodItem, Store store, 
                                                User user, Survey survey) {
        return CustomerFeedback.builder()
                .foodItem(foodItem)
                .store(store)
                .user(user)
                .survey(survey)
                .build();
    }
    
    public static CustomerFeedback createFeedbackWithSatisfaction(FoodItem foodItem, Store store, 
                                                User user, Survey survey, Float satisfaction) {
        return CustomerFeedback.builder()
                .foodItem(foodItem)
                .store(store)
                .user(user)
                .survey(survey)
                .overallSatisfaction(satisfaction)
                .build();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return this.isActive != null && this.isActive;
    }
}