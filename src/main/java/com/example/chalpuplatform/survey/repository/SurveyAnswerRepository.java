package com.example.chalpuplatform.survey.repository;

import com.example.chalpuplatform.jar.domain.JARDataPoint;
import com.example.chalpuplatform.survey.domain.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    List<SurveyAnswer> findByFeedbackIdOrderByQuestionId(Long feedbackId);

    Optional<SurveyAnswer> findByFeedbackIdAndQuestionId(Long feedbackId, Long questionId);

    List<SurveyAnswer> findByQuestionId(Long questionId);
    
    void deleteByFeedbackId(Long feedbackId);
    
    // JAR 분석용 쿼리
    @Query("""
        SELECT sa.question.id, sa.question.jarAttribute, sa.numericValue, 
               (SELECT osa.numericValue FROM SurveyAnswer osa 
                WHERE osa.feedback.id = f.id 
                AND osa.question.id = 10)
        FROM SurveyAnswer sa
        JOIN sa.feedback f
        JOIN sa.question sq
        WHERE sq.jarAttribute IS NOT NULL
        AND f.store.id = :storeId
        AND f.createdAt BETWEEN :startDate AND :endDate
        AND EXISTS (SELECT 1 FROM SurveyAnswer osa2 
                    WHERE osa2.feedback.id = f.id 
                    AND osa2.question.id = 10 
                    AND osa2.numericValue IS NOT NULL)
        """)
    List<Object[]> findJARDataByStore(@Param("storeId") Long storeId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("""
        SELECT sa.question.id, sa.question.jarAttribute, sa.numericValue,
               (SELECT osa.numericValue FROM SurveyAnswer osa 
                WHERE osa.feedback.id = f.id 
                AND osa.question.id = 10)
        FROM SurveyAnswer sa
        JOIN sa.feedback f
        JOIN sa.question sq
        WHERE sq.jarAttribute IS NOT NULL
        AND f.foodItem.id = :foodItemId
        AND f.createdAt BETWEEN :startDate AND :endDate
        AND EXISTS (SELECT 1 FROM SurveyAnswer osa2 
                    WHERE osa2.feedback.id = f.id 
                    AND osa2.question.id = 10 
                    AND osa2.numericValue IS NOT NULL)
        """)
    List<Object[]> findJARDataByFoodItem(@Param("foodItemId") Long foodItemId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("""
        SELECT new com.example.chalpuplatform.jar.domain.JARDataPoint(
            sa.numericValue, 
            (SELECT osa.numericValue FROM SurveyAnswer osa 
             WHERE osa.feedback.id = sa.feedback.id 
             AND osa.question.id = 10)
        )
        FROM SurveyAnswer sa
        WHERE sa.question.id = :questionId
        AND sa.question.jarAttribute IS NOT NULL
        AND EXISTS (SELECT 1 FROM SurveyAnswer osa2 
                    WHERE osa2.feedback.id = sa.feedback.id 
                    AND osa2.question.id = 10 
                    AND osa2.numericValue IS NOT NULL)
        """)
    List<JARDataPoint> findJARDataByQuestion(@Param("questionId") Long questionId);
    
    // NPS 데이터 조회
    @Query("""
        SELECT sa.feedback.id, sa.question.questionType, sa.numericValue
        FROM SurveyAnswer sa
        WHERE sa.question.questionType IN ('NPS_RECOMMEND', 'NPS_REORDER')
        AND sa.feedback.foodItem.id = :foodItemId
        AND sa.feedback.createdAt BETWEEN :startDate AND :endDate
        AND sa.numericValue IS NOT NULL
        ORDER BY sa.feedback.id, sa.question.questionType
        """)
    List<Object[]> findNPSDataByFoodItem(@Param("foodItemId") Long foodItemId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}