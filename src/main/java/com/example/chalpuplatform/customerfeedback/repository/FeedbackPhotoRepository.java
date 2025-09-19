package com.example.chalpuplatform.customerfeedback.repository;

import com.example.chalpuplatform.customerfeedback.domain.FeedbackPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackPhotoRepository extends JpaRepository<FeedbackPhoto, Long> {

    List<FeedbackPhoto> findByFeedbackIdOrderByCreatedAtAsc(Long feedbackId);

    void deleteByFeedbackId(Long feedbackId);

    int countByFeedbackId(Long feedbackId);

    // 배치 조회 - 여러 피드백의 사진을 한번에 조회
    @Query("""
        SELECT fp FROM FeedbackPhoto fp
        WHERE fp.feedback.id IN :feedbackIds
        ORDER BY fp.feedback.id, fp.createdAt
    """)
    List<FeedbackPhoto> findPhotosByFeedbackIds(@Param("feedbackIds") List<Long> feedbackIds);
}