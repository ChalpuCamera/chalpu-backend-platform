package com.example.chalpuplatform.survey.repository;

import com.example.chalpuplatform.jar.domain.JARAttribute;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    List<SurveyQuestion> findBySurveyIdOrderById(Long surveyId);

    int countBySurveyId(Long surveyId);

    List<SurveyQuestion> findByJarAttributeIn(List<JARAttribute> jarAttributes);

    Optional<SurveyQuestion> findByJarAttribute(JARAttribute jarAttribute);

    List<SurveyQuestion> findBySurveyIdAndJarAttributeIn(Long surveyId, List<JARAttribute> jarAttributes);

    Optional<SurveyQuestion> findBySurveyIdAndJarAttribute(Long surveyId, JARAttribute jarAttribute);
}