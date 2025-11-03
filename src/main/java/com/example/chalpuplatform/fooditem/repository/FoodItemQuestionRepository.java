package com.example.chalpuplatform.fooditem.repository;

import com.example.chalpuplatform.fooditem.domain.FoodItem;
import com.example.chalpuplatform.fooditem.domain.FoodItemQuestion;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemQuestionRepository extends JpaRepository<FoodItemQuestion, Long> {

    List<FoodItemQuestion> findByFoodItemAndIsActiveTrue(FoodItem foodItem);

    @Query("SELECT fiq FROM FoodItemQuestion fiq " +
           "JOIN FETCH fiq.question " +
           "WHERE fiq.foodItem.id = :foodItemId AND fiq.isActive = true")
    List<FoodItemQuestion> findActiveQuestionsByFoodItemId(@Param("foodItemId") Long foodItemId);

    boolean existsByFoodItemAndQuestionAndIsActiveTrue(FoodItem foodItem, SurveyQuestion question);

    Optional<FoodItemQuestion> findByFoodItemAndQuestion(FoodItem foodItem, SurveyQuestion question);

    @Query("SELECT COUNT(fiq) FROM FoodItemQuestion fiq " +
           "WHERE fiq.foodItem.id = :foodItemId AND fiq.isActive = true")
    long countActiveQuestionsByFoodItemId(@Param("foodItemId") Long foodItemId);

    @Modifying
    void deleteByFoodItem(FoodItem foodItem);
}
