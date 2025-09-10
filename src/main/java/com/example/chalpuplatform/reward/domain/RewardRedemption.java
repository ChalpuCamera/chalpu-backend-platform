package com.example.chalpuplatform.reward.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reward_redemptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class RewardRedemption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "redemption_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "reward_count")
    private Integer rewardCount;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RedemptionStatus status;

    public static RewardRedemption createRedemption(User user, Reward reward, Integer rewardCount) {
        return RewardRedemption.builder()
                .user(user)
                .reward(reward)
                .rewardCount(rewardCount)
                .status(RedemptionStatus.ISSUED)
                .build();
    }

    public void markAsUsed() {
        this.status = RedemptionStatus.USED;
    }

    public void cancel() {
        this.status = RedemptionStatus.CANCELLED;
    }

    public boolean isActive() {
        return this.status == RedemptionStatus.ISSUED;
    }

    public enum RedemptionStatus {
        ISSUED, USED, CANCELLED
    }
}