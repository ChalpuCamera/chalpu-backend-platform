package com.example.chalpuplatform.survey.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.SurveyException;
import com.example.chalpuplatform.jar.domain.JARAttribute;
import com.example.chalpuplatform.survey.domain.QuestionType;
import com.example.chalpuplatform.survey.domain.Survey;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import com.example.chalpuplatform.survey.repository.SurveyQuestionRepository;
import com.example.chalpuplatform.survey.repository.SurveyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SurveyQuestionService 테스트")
class SurveyQuestionServiceTest {

    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @Mock
    private SurveyRepository surveyRepository;

    @InjectMocks
    private SurveyQuestionService surveyQuestionService;

    private Survey survey;
    private SurveyQuestion question1;
    private SurveyQuestion question2;
    private SurveyQuestion question3;

    @BeforeEach
    void setUp() {
        survey = Survey.builder()
                .id(1L)
                .surveyName("테스트 서베이")
                .build();

        question1 = SurveyQuestion.builder()
                .id(1L)
                .survey(survey)
                .questionText("맵기는 어떠셨나요?")
                .questionType(QuestionType.SLIDER)
                .jarAttribute(JARAttribute.SPICINESS)
                .build();

        question2 = SurveyQuestion.builder()
                .id(2L)
                .survey(survey)
                .questionText("단맛은 어떠셨나요?")
                .questionType(QuestionType.SLIDER)
                .jarAttribute(JARAttribute.SWEETNESS)
                .build();

        question3 = SurveyQuestion.builder()
                .id(3L)
                .survey(survey)
                .questionText("사장님께 한마디")
                .questionType(QuestionType.TEXT)
                .jarAttribute(JARAttribute.OWNER_MESSAGE)
                .build();
    }

    @Nested
    @DisplayName("getQuestionsBySurveyId 메서드는")
    class GetQuestionsBySurveyId {

        @Test
        @DisplayName("서베이의 모든 질문을 반환한다")
        void returnAllQuestions() {
            List<SurveyQuestion> questions = Arrays.asList(question1, question2, question3);

            given(surveyRepository.findById(1L)).willReturn(Optional.of(survey));
            given(surveyQuestionRepository.findBySurveyIdOrderById(1L)).willReturn(questions);

            List<SurveyQuestion> result = surveyQuestionService.getQuestionsBySurveyId(1L);

            assertThat(result).hasSize(3);
            assertThat(result).extracting(SurveyQuestion::getId).containsExactly(1L, 2L, 3L);
            assertThat(result).extracting(SurveyQuestion::getQuestionText)
                    .contains("맵기는 어떠셨나요?", "단맛은 어떠셨나요?", "사장님께 한마디");
        }

        @Test
        @DisplayName("존재하지 않는 서베이면 예외를 발생시킨다")
        void throwException_WhenSurveyNotFound() {
            given(surveyRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> surveyQuestionService.getQuestionsBySurveyId(999L))
                    .isInstanceOf(SurveyException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.SURVEY_NOT_FOUND);
        }

        @Test
        @DisplayName("질문이 없는 서베이는 빈 리스트를 반환한다")
        void returnEmptyList_WhenNoQuestions() {
            given(surveyRepository.findById(1L)).willReturn(Optional.of(survey));
            given(surveyQuestionRepository.findBySurveyIdOrderById(1L)).willReturn(List.of());

            List<SurveyQuestion> result = surveyQuestionService.getQuestionsBySurveyId(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("질문들이 ID 순서대로 정렬되어 반환된다")
        void returnQuestionsOrderedById() {
            List<SurveyQuestion> questions = Arrays.asList(question1, question2, question3);

            given(surveyRepository.findById(1L)).willReturn(Optional.of(survey));
            given(surveyQuestionRepository.findBySurveyIdOrderById(1L)).willReturn(questions);

            List<SurveyQuestion> result = surveyQuestionService.getQuestionsBySurveyId(1L);

            assertThat(result).extracting(SurveyQuestion::getId).containsExactly(1L, 2L, 3L);
        }
    }
}
