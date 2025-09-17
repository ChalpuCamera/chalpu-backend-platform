package com.example.chalpuplatform.fooditem.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "menu_extraction_progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuExtractionProgress extends BaseTimeEntity {

    @Id
    @Column(name = "request_id", length = 50)
    private String requestId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage;  // 0-100

    @Column(name = "current_step", length = 500)
    private String currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExtractionStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "extracted_count")
    private Integer extractedCount;  // 추출된 FoodItem 개수

    public enum ExtractionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public static MenuExtractionProgress createInitialProgress(String requestId, Long storeId) {
        return MenuExtractionProgress.builder()
                .requestId(requestId)
                .storeId(storeId)
                .progressPercentage(0)
                .currentStep("처리 대기 중")
                .status(ExtractionStatus.PENDING)
                .build();
    }

    public void updateProgress(Integer progressPercentage, String currentStep) {
        this.progressPercentage = progressPercentage;
        this.currentStep = currentStep;
        if (progressPercentage > 0 && this.status == ExtractionStatus.PENDING) {
            this.status = ExtractionStatus.PROCESSING;
        }
    }

    public void complete(Integer extractedCount) {
        this.progressPercentage = 100;
        this.currentStep = "처리 완료";
        this.status = ExtractionStatus.COMPLETED;
        this.extractedCount = extractedCount;
        // updatedAt은 자동으로 업데이트됨
    }

    public void fail(String errorMessage) {
        this.progressPercentage = -1;
        this.currentStep = "처리 실패";
        this.status = ExtractionStatus.FAILED;
        this.errorMessage = errorMessage;
        // updatedAt은 자동으로 업데이트됨
    }
}