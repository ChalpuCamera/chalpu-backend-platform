package com.example.chalpuplatform.fooditem.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.fooditem.dto.ActiveQuestionResponse;
import com.example.chalpuplatform.fooditem.dto.FoodItemQuestionUpdateRequest;
import com.example.chalpuplatform.fooditem.service.FoodItemQuestionService;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fooditems")
@Tag(name = "Food Item Question", description = "메뉴별 질문 관리 API")
public class FoodItemQuestionController {

    private final FoodItemQuestionService foodItemQuestionService;

    @PutMapping("/{foodItemId}/questions")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "메뉴별 활성화 질문 설정",
            description = "특정 메뉴에 대해 활성화할 JAR 질문들을 설정합니다. 사장님께 한마디는 자동으로 포함됩니다. 매장 관리 권한이 필요합니다."
    )
    public ResponseEntity<ApiResponse<Void>> updateActiveQuestions(
            @PathVariable("foodItemId") Long foodItemId,
            @RequestBody @Valid FoodItemQuestionUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        foodItemQuestionService.activateQuestionsForFoodItem(
                foodItemId,
                request.getSurveyId(),
                request.getJarAttributes(),
                userDetails.getId()
        );

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{foodItemId}/active-questions")
    @Operation(
            summary = "메뉴별 활성화된 질문 조회",
            description = "특정 메뉴에 대해 소비자가 답변 가능한 활성화된 질문 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<ActiveQuestionResponse>>> getActiveQuestions(
            @PathVariable("foodItemId") Long foodItemId) {

        List<SurveyQuestion> questions = foodItemQuestionService.getActiveQuestionsForFoodItem(foodItemId);

        List<ActiveQuestionResponse> responses = questions.stream()
                .map(ActiveQuestionResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
