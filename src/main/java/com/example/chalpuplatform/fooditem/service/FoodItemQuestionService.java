package com.example.chalpuplatform.fooditem.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.domain.FoodItemQuestion;
import com.example.chalpuplatform.fooditem.repository.FoodItemQuestionRepository;
import com.example.chalpuplatform.fooditem.repository.FoodItemRepository;
import com.example.chalpuplatform.jar.domain.JARAttribute;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import com.example.chalpuplatform.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FoodItemQuestionService {

    private final FoodItemQuestionRepository foodItemQuestionRepository;
    private final FoodItemRepository foodItemRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;

    public void activateQuestionsForFoodItem(Long foodItemId, Long surveyId, List<JARAttribute> jarAttributes, Long userId) {
        try {
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.FOOD_NOT_FOUND));

            userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, foodItem.getStore().getId())
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

            foodItemQuestionRepository.deleteByFoodItem(foodItem);

            List<SurveyQuestion> questions = surveyQuestionRepository.findBySurveyIdAndJarAttributeIn(surveyId, jarAttributes);

            SurveyQuestion ownerMessageQuestion = surveyQuestionRepository.findBySurveyIdAndJarAttribute(surveyId, JARAttribute.OWNER_MESSAGE)
                    .orElseThrow(() -> new StoreException(ErrorMessage.INVALID_PARAMETER));

            if (!questions.contains(ownerMessageQuestion)) {
                questions.add(ownerMessageQuestion);
            }

            List<FoodItemQuestion> foodItemQuestions = questions.stream()
                    .map(question -> FoodItemQuestion.builder()
                            .foodItem(foodItem)
                            .question(question)
                            .isActive(true)
                            .build())
                    .collect(Collectors.toList());

            foodItemQuestionRepository.saveAll(foodItemQuestions);

            log.info("event=food_item_questions_activated, food_item_id={}, survey_id={}, question_count={}",
                    foodItemId, surveyId, foodItemQuestions.size());
        } catch (StoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=food_item_questions_activation_failed, food_item_id={}, survey_id={}, error_message={}",
                    foodItemId, surveyId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<SurveyQuestion> getActiveQuestionsForFoodItem(Long foodItemId) {
        try {
            List<FoodItemQuestion> foodItemQuestions = foodItemQuestionRepository
                    .findActiveQuestionsByFoodItemId(foodItemId);

            return foodItemQuestions.stream()
                    .map(FoodItemQuestion::getQuestion)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("event=get_active_questions_failed, food_item_id={}, error_message={}",
                    foodItemId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    public void deactivateQuestion(Long foodItemId, Long questionId, Long userId) {
        try {
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.FOOD_NOT_FOUND));

            userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, foodItem.getStore().getId())
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

            SurveyQuestion question = surveyQuestionRepository.findById(questionId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.INVALID_PARAMETER));

            if (question.isOwnerMessageQuestion()) {
                throw new StoreException(ErrorMessage.INVALID_PARAMETER);
            }

            FoodItemQuestion foodItemQuestion = foodItemQuestionRepository
                    .findByFoodItemAndQuestion(foodItem, question)
                    .orElseThrow(() -> new StoreException(ErrorMessage.INVALID_PARAMETER));

            foodItemQuestion.deactivate();

            log.info("event=food_item_question_deactivated, food_item_id={}, question_id={}",
                    foodItemId, questionId);
        } catch (StoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=food_item_question_deactivation_failed, food_item_id={}, question_id={}, error_message={}",
                    foodItemId, questionId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
