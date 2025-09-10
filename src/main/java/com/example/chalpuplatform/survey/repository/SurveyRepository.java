package com.example.chalpuplatform.survey.repository;

import com.example.chalpuplatform.survey.domain.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Optional<Survey> findBySurveyName(String surveyName);
}