package com.example.chalpuplatform.customerfeedback.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.survey.domain.Survey;
import com.example.chalpuplatform.campaign.domain.Campaign;
import jakarta.persistence.*;
import lombok.*;

@NamedEntityGraph(
    name = "CustomerFeedback.detail",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("store"),
        @NamedAttributeNode("foodItem"),
        @NamedAttributeNode("survey"),
        @NamedAttributeNode("campaign")
    }
)
@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = true)
    private Campaign campaign;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.SUBMITTED;
    
    @Column(name = "overall_satisfaction")
    private Float overallSatisfaction;

    @Builder.Default
    @Column(name = "is_viewed")
    private Boolean isViewed = false;

    // 피드백 작성 시점의 고객 입맛 스냅샷
    @Column(name = "spicy_level_snapshot")
    private Integer spicyLevelSnapshot;

    @Column(name = "meal_amount_snapshot")
    private Integer mealAmountSnapshot;

    @Column(name = "meal_spending_snapshot")
    private Integer mealSpendingSnapshot;

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

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isActive() {
        return this.isActive != null && this.isActive;
    }

    public void markAsViewed() {
        this.isViewed = true;
    }

    public boolean isViewed() {
        return this.isViewed != null && this.isViewed;
    }
}