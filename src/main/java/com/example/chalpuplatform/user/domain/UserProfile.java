package com.example.chalpuplatform.user.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "feedback_count")
    private Integer feedbackCount = 0;

    @Builder.Default
    @Column(name = "reward_count")
    private Integer rewardCount = 0;

    @Embedded
    private CustomerTaste customerTaste;

    // 정적 팩토리 메서드
    public static UserProfile create(User user) {
        return UserProfile.builder()
                .user(user)
                .feedbackCount(0)
                .rewardCount(0)
                .build();
    }

    public void incrementFeedbackCount() {
        this.feedbackCount = this.feedbackCount + 1;
    }

    public void incrementRewardCount() {
        this.rewardCount = this.rewardCount + 1;
    }

    public void decrementRewardCount(Integer count) {
        if (this.rewardCount < count) {
            throw new IllegalArgumentException("보유 리워드 횟수가 부족합니다.");
        }
        this.rewardCount = this.rewardCount - count;
    }
}