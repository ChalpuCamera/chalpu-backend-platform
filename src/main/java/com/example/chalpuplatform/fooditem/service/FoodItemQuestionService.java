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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    public void activateQuestionsForFoodItem(Long foodItemId, List<Long> questionIds, Long userId) {
        try {
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.FOOD_NOT_FOUND));

            userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(userId, foodItem.getStore().getId())
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

            List<SurveyQuestion> newQuestions = surveyQuestionRepository.findAllById(questionIds);

            if (newQuestions.size() != questionIds.size()) {
                throw new StoreException(ErrorMessage.INVALID_PARAMETER);
            }

            boolean hasOwnerMessage = newQuestions.stream()
                    .anyMatch(SurveyQuestion::isOwnerMessageQuestion);

            if (!hasOwnerMessage) {
                throw new StoreException(ErrorMessage.SURVEY_MUST_CONTAIN_FORCEOMESSAGE);
            }

            List<FoodItemQuestion> existingMappings = foodItemQuestionRepository.findByFoodItem(foodItem);

            Set<Long> existingQuestionIds = existingMappings.stream()
                    .map(fiq -> fiq.getQuestion().getId())
                    .collect(Collectors.toSet());

            Set<Long> newQuestionIds = new HashSet<>(questionIds);

            Set<Long> toDelete = existingQuestionIds.stream()
                    .filter(id -> !newQuestionIds.contains(id))
                    .collect(Collectors.toSet());

            Set<Long> toAdd = newQuestionIds.stream()
                    .filter(id -> !existingQuestionIds.contains(id))
                    .collect(Collectors.toSet());

            if (!toDelete.isEmpty()) {
                List<FoodItemQuestion> toDeleteList = existingMappings.stream()
                        .filter(fiq -> toDelete.contains(fiq.getQuestion().getId()))
                        .collect(Collectors.toList());
                foodItemQuestionRepository.deleteAll(toDeleteList);
            }

            if (!toAdd.isEmpty()) {
                List<SurveyQuestion> questionsToAdd = newQuestions.stream()
                        .filter(q -> toAdd.contains(q.getId()))
                        .toList();

                List<FoodItemQuestion> foodItemQuestions = questionsToAdd.stream()
                        .map(question -> FoodItemQuestion.builder()
                                .foodItem(foodItem)
                                .question(question)
                                .build())
                        .collect(Collectors.toList());

                foodItemQuestionRepository.saveAll(foodItemQuestions);
            }

            log.info("event=food_item_questions_activated, food_item_id={}, added={}, deleted={}, total={}",
                    foodItemId, toAdd.size(), toDelete.size(), newQuestionIds.size());
        } catch (StoreException e) {
            throw e;
        } catch (Exception e) {
            log.error("event=food_item_questions_activation_failed, food_item_id={}, error_message={}",
                    foodItemId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<SurveyQuestion> getActiveQuestionsForFoodItem(Long foodItemId) {
        try {
            List<FoodItemQuestion> foodItemQuestions = foodItemQuestionRepository
                    .findByFoodItemId(foodItemId);

            return foodItemQuestions.stream()
                    .map(FoodItemQuestion::getQuestion)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("event=get_active_questions_failed, food_item_id={}, error_message={}",
                    foodItemId, e.getMessage(), e);
            throw new StoreException(ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
