package com.example.chalpuplatform.campaign.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NamedEntityGraph(
    name = "Campaign.withStoreAndFoodItem",
    attributeNodes = {
        @NamedAttributeNode("store"),
        @NamedAttributeNode("foodItem")
    }
)
@Entity
@Table(name = "campaigns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Campaign extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    @Column(name = "target_feedback_count", nullable = false)
    private Integer targetFeedbackCount;

    @Column(name = "current_feedback_count", nullable = false)
    @Builder.Default
    private Integer currentFeedbackCount = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public void activate() {
        validateActivation();
        this.status = CampaignStatus.ACTIVE;
    }

    public void pause() {
        if (this.status != CampaignStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 캠페인만 일시 중지할 수 있습니다");
        }
        this.status = CampaignStatus.PAUSED;
    }

    public void complete() {
        if (this.status == CampaignStatus.COMPLETED) {
            return;
        }
        this.status = CampaignStatus.COMPLETED;
    }

    public void expire() {
        if (this.status == CampaignStatus.COMPLETED || this.status == CampaignStatus.EXPIRED) {
            return;
        }
        this.status = CampaignStatus.EXPIRED;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public boolean isActive() {
        return this.isActive != null && this.isActive
            && this.status == CampaignStatus.ACTIVE
            && LocalDateTime.now().isBefore(this.endDate)
            && LocalDateTime.now().isAfter(this.startDate);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.endDate);
    }

    public boolean canBeModified() {
        return this.status == CampaignStatus.DRAFT || this.status == CampaignStatus.PAUSED;
    }

    public void validateActivation() {
        if (this.status != CampaignStatus.DRAFT && this.status != CampaignStatus.PAUSED) {
            throw new IllegalStateException("초안 또는 일시 중지 상태의 캠페인만 활성화할 수 있습니다");
        }

        if (LocalDateTime.now().isAfter(this.endDate)) {
            throw new IllegalStateException("종료일이 지난 캠페인은 활성화할 수 없습니다");
        }

        if (this.targetFeedbackCount == null || this.targetFeedbackCount <= 0) {
            throw new IllegalStateException("목표 피드백 수는 0보다 커야 합니다");
        }
    }

    public void updateCampaign(String name, String description, Integer targetFeedbackCount,
                              LocalDateTime startDate, LocalDateTime endDate) {
        if (!canBeModified()) {
            throw new IllegalStateException("활성 또는 완료된 캠페인은 수정할 수 없습니다");
        }

        this.name = name;
        this.description = description;
        this.targetFeedbackCount = targetFeedbackCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isTargetReached(long currentFeedbackCount) {
        return currentFeedbackCount >= this.targetFeedbackCount;
    }

    public enum CampaignStatus {
        DRAFT("초안"),
        ACTIVE("활성"),
        PAUSED("일시중지"),
        COMPLETED("완료"),
        EXPIRED("만료");

        private final String korean;

        CampaignStatus(String korean) {
            this.korean = korean;
        }

        public String getKorean() {
            return korean;
        }
    }
}