package com.example.chalpuplatform.customerfeedback.repository;

import com.example.chalpuplatform.customerfeedback.domain.FeedbackPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackPhotoRepository extends JpaRepository<FeedbackPhoto, Long> {

    List<FeedbackPhoto> findByFeedbackIdOrderByCreatedAtAsc(Long feedbackId);
    
    void deleteByFeedbackId(Long feedbackId);
    
    int countByFeedbackId(Long feedbackId);
}