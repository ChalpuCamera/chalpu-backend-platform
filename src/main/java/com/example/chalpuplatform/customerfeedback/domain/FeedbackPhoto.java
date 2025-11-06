package com.example.chalpuplatform.customerfeedback.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "feedback_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class FeedbackPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private CustomerFeedback feedback;

    @Column(name = "s3_key", length = 500, nullable = false)
    private String s3Key;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    public static FeedbackPhoto createFeedbackPhoto(CustomerFeedback feedback, String s3Key, String fileName) {
        return FeedbackPhoto.builder()
                .feedback(feedback)
                .s3Key(s3Key)
                .fileName(fileName)
                .build();
    }
}