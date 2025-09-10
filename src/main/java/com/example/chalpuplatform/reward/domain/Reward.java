package com.example.chalpuplatform.reward.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "rewards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Reward extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reward_id")
    private Long id;

    @Column(name = "reward_name", length = 100, nullable = false)
    private String rewardName;

    @Column(name = "reward_type", length = 50)
    private String rewardType;

    @Column(name = "reward_value")
    private Integer rewardValue;

    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    public static Reward createReward(String rewardName, String rewardType, Integer rewardValue, 
                                    Integer requiredCount, String description, LocalDate expiryDate) {
        return Reward.builder()
                .rewardName(rewardName)
                .rewardType(rewardType)
                .rewardValue(rewardValue)
                .requiredCount(requiredCount)
                .description(description)
                .expiryDate(expiryDate)
                .build();
    }

    public void updateReward(String rewardName, String rewardType, Integer rewardValue, 
                           Integer requiredCount, String description, LocalDate expiryDate) {
        this.rewardName = rewardName;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.requiredCount = requiredCount;
        this.description = description;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        if (this.expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(this.expiryDate);
    }

    public boolean isAvailable() {
        return !isExpired();
    }
}