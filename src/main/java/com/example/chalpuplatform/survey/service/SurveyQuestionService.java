package com.example.chalpuplatform.survey.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.SurveyException;
import com.example.chalpuplatform.survey.domain.Survey;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import com.example.chalpuplatform.survey.repository.SurveyQuestionRepository;
import com.example.chalpuplatform.survey.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyQuestionService {

    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyRepository surveyRepository;

    public List<SurveyQuestion> getQuestionsBySurveyId(Long surveyId) {
        try {
            Survey survey = surveyRepository.findById(surveyId)
                    .orElseThrow(() -> new SurveyException(ErrorMessage.SURVEY_NOT_FOUND));

            List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyIdOrderById(surveyId);

            log.info("event=survey_questions_retrieved, survey_id={}, question_count={}",
                    surveyId, questions.size());

            return questions;
        } catch (SurveyException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=survey_questions_retrieval_failed, survey_id={}, error_message={}",
                    surveyId, e.getMessage(), e);
            throw new SurveyException(ErrorMessage.SURVEY_NOT_FOUND);
        }
    }
}
