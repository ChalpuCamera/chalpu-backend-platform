package com.example.chalpuplatform.fooditem.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.domain.FoodItemQuestion;
import com.example.chalpuplatform.fooditem.repository.FoodItemQuestionRepository;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.jar.domain.JARAttribute;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.survey.domain.QuestionType;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.survey.domain.Survey;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import com.example.chalpuplatform.survey.repository.SurveyQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FoodItemQuestionService 테스트")
class FoodItemQuestionServiceTest {

    @Mock
    private FoodItemQuestionRepository foodItemQuestionRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private SurveyQuestionRepository surveyQuestionRepository;

    @Mock
    private UserStoreRoleRepository userStoreRoleRepository;

    @InjectMocks
    private FoodItemQuestionService foodItemQuestionService;

    private FoodItem foodItem;
    private Store store;
    private UserStoreRole userStoreRole;
    private Survey survey;
    private SurveyQuestion ownerMessageQuestion;
    private SurveyQuestion spicinessQuestion;
    private SurveyQuestion sweetnessQuestion;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .id(1L)
                .storeName("테스트 매장")
                .build();

        foodItem = FoodItem.builder()
                .id(1L)
                .store(store)
                .foodName("테스트 메뉴")
                .build();

        User user = User.builder()
                .id(1L)
                .build();

        userStoreRole = UserStoreRole.builder()
                .id(1L)
                .user(user)
                .store(store)
                .build();

        survey = Survey.builder()
                .id(1L)
                .surveyName("테스트 서베이")
                .build();

        ownerMessageQuestion = SurveyQuestion.builder()
                .id(1L)
                .survey(survey)
                .questionText("사장님께 한마디")
                .questionType(QuestionType.TEXT)
                .jarAttribute(JARAttribute.OWNER_MESSAGE)
                .build();

        spicinessQuestion = SurveyQuestion.builder()
                .id(2L)
                .survey(survey)
                .questionText("맵기는 어떠셨나요?")
                .questionType(QuestionType.SLIDER)
                .jarAttribute(JARAttribute.SPICINESS)
                .build();

        sweetnessQuestion = SurveyQuestion.builder()
                .id(3L)
                .survey(survey)
                .questionText("단맛은 어떠셨나요?")
                .questionType(QuestionType.SLIDER)
                .jarAttribute(JARAttribute.SWEETNESS)
                .build();
    }

    @Nested
    @DisplayName("activateQuestionsForFoodItem 메서드는")
    class ActivateQuestionsForFoodItem {

        @Test
        @DisplayName("새로운 질문들을 활성화할 수 있다")
        void activateNewQuestions() {
            List<Long> questionIds = Arrays.asList(1L, 2L, 3L);
            List<SurveyQuestion> questions = Arrays.asList(ownerMessageQuestion, spicinessQuestion, sweetnessQuestion);

            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.of(userStoreRole));
            given(surveyQuestionRepository.findAllById(questionIds)).willReturn(questions);
            given(foodItemQuestionRepository.findByFoodItem(foodItem)).willReturn(new ArrayList<>());

            foodItemQuestionService.activateQuestionsForFoodItem(1L, questionIds, 1L);

            verify(foodItemQuestionRepository).saveAll(anyList());
            verify(foodItemQuestionRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("증분 업데이트 - 일부 질문 추가")
        void incrementalUpdate_Add() {
            FoodItemQuestion existingMapping1 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(ownerMessageQuestion)
                    .build();

            FoodItemQuestion existingMapping2 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(spicinessQuestion)
                    .build();

            List<FoodItemQuestion> existingMappings = Arrays.asList(existingMapping1, existingMapping2);
            List<Long> newQuestionIds = Arrays.asList(1L, 2L, 3L);
            List<SurveyQuestion> newQuestions = Arrays.asList(ownerMessageQuestion, spicinessQuestion, sweetnessQuestion);

            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.of(userStoreRole));
            given(surveyQuestionRepository.findAllById(newQuestionIds)).willReturn(newQuestions);
            given(foodItemQuestionRepository.findByFoodItem(foodItem)).willReturn(existingMappings);

            foodItemQuestionService.activateQuestionsForFoodItem(1L, newQuestionIds, 1L);

            verify(foodItemQuestionRepository).saveAll(argThat(list -> {
                List<FoodItemQuestion> questionList = (List<FoodItemQuestion>) list;
                return questionList.size() == 1;
            }));
            verify(foodItemQuestionRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("증분 업데이트 - 일부 질문 삭제")
        void incrementalUpdate_Delete() {
            FoodItemQuestion existingMapping1 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(ownerMessageQuestion)
                    .build();

            FoodItemQuestion existingMapping2 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(spicinessQuestion)
                    .build();

            FoodItemQuestion existingMapping3 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(sweetnessQuestion)
                    .build();

            List<FoodItemQuestion> existingMappings = Arrays.asList(existingMapping1, existingMapping2, existingMapping3);
            List<Long> newQuestionIds = Arrays.asList(1L, 2L);
            List<SurveyQuestion> newQuestions = Arrays.asList(ownerMessageQuestion, spicinessQuestion);

            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.of(userStoreRole));
            given(surveyQuestionRepository.findAllById(newQuestionIds)).willReturn(newQuestions);
            given(foodItemQuestionRepository.findByFoodItem(foodItem)).willReturn(existingMappings);

            foodItemQuestionService.activateQuestionsForFoodItem(1L, newQuestionIds, 1L);

            verify(foodItemQuestionRepository).deleteAll(argThat(list -> {
                List<FoodItemQuestion> questionList = (List<FoodItemQuestion>) list;
                return questionList.size() == 1;
            }));
            verify(foodItemQuestionRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("변경사항이 없으면 DB 작업을 하지 않는다")
        void noChanges_NoDbOperation() {
            FoodItemQuestion existingMapping1 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(ownerMessageQuestion)
                    .build();

            FoodItemQuestion existingMapping2 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(spicinessQuestion)
                    .build();

            List<FoodItemQuestion> existingMappings = Arrays.asList(existingMapping1, existingMapping2);
            List<Long> newQuestionIds = Arrays.asList(1L, 2L);
            List<SurveyQuestion> newQuestions = Arrays.asList(ownerMessageQuestion, spicinessQuestion);

            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.of(userStoreRole));
            given(surveyQuestionRepository.findAllById(newQuestionIds)).willReturn(newQuestions);
            given(foodItemQuestionRepository.findByFoodItem(foodItem)).willReturn(existingMappings);

            foodItemQuestionService.activateQuestionsForFoodItem(1L, newQuestionIds, 1L);

            verify(foodItemQuestionRepository, never()).saveAll(anyList());
            verify(foodItemQuestionRepository, never()).deleteAll(anyList());
        }

        @Test
        @DisplayName("OWNER_MESSAGE 질문이 없으면 예외를 발생시킨다")
        void throwException_WhenOwnerMessageNotIncluded() {
            List<Long> questionIds = Arrays.asList(2L, 3L);
            List<SurveyQuestion> questions = Arrays.asList(spicinessQuestion, sweetnessQuestion);

            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.of(userStoreRole));
            given(surveyQuestionRepository.findAllById(questionIds)).willReturn(questions);

            assertThatThrownBy(() -> foodItemQuestionService.activateQuestionsForFoodItem(1L, questionIds, 1L))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.SURVEY_MUST_CONTAIN_FORCEOMESSAGE);
        }

        @Test
        @DisplayName("존재하지 않는 FoodItem이면 예외를 발생시킨다")
        void throwException_WhenFoodItemNotFound() {
            List<Long> questionIds = Arrays.asList(1L, 2L);

            given(foodItemRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> foodItemQuestionService.activateQuestionsForFoodItem(1L, questionIds, 1L))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.FOOD_NOT_FOUND);
        }

        @Test
        @DisplayName("권한이 없는 사용자면 예외를 발생시킨다")
        void throwException_WhenUserUnauthorized() {
            List<Long> questionIds = Arrays.asList(1L, 2L);

            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> foodItemQuestionService.activateQuestionsForFoodItem(1L, questionIds, 1L))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.STORE_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 질문 ID가 포함되면 예외를 발생시킨다")
        void throwException_WhenQuestionNotFound() {
            List<Long> questionIds = Arrays.asList(1L, 2L, 999L);
            List<SurveyQuestion> questions = Arrays.asList(ownerMessageQuestion, spicinessQuestion);

            given(foodItemRepository.findById(1L)).willReturn(Optional.of(foodItem));
            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.of(userStoreRole));
            given(surveyQuestionRepository.findAllById(questionIds)).willReturn(questions);

            assertThatThrownBy(() -> foodItemQuestionService.activateQuestionsForFoodItem(1L, questionIds, 1L))
                    .isInstanceOf(StoreException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.INVALID_PARAMETER);
        }
    }

    @Nested
    @DisplayName("getActiveQuestionsForFoodItem 메서드는")
    class GetActiveQuestionsForFoodItem {

        @Test
        @DisplayName("활성화된 질문 목록을 반환한다")
        void returnActiveQuestions() {
            FoodItemQuestion mapping1 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(ownerMessageQuestion)
                    .build();

            FoodItemQuestion mapping2 = FoodItemQuestion.builder()
                    .foodItem(foodItem)
                    .question(spicinessQuestion)
                    .build();

            List<FoodItemQuestion> mappings = Arrays.asList(mapping1, mapping2);

            given(foodItemQuestionRepository.findByFoodItemId(1L)).willReturn(mappings);

            List<SurveyQuestion> result = foodItemQuestionService.getActiveQuestionsForFoodItem(1L);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(SurveyQuestion::getId).containsExactly(1L, 2L);
        }

        @Test
        @DisplayName("활성화된 질문이 없으면 빈 리스트를 반환한다")
        void returnEmptyList_WhenNoActiveQuestions() {
            given(foodItemQuestionRepository.findByFoodItemId(1L)).willReturn(new ArrayList<>());

            List<SurveyQuestion> result = foodItemQuestionService.getActiveQuestionsForFoodItem(1L);

            assertThat(result).isEmpty();
        }
    }
}
