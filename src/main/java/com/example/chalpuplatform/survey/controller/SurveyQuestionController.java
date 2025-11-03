package com.example.chalpuplatform.survey.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.survey.domain.SurveyQuestion;
import com.example.chalpuplatform.survey.dto.SurveyQuestionResponse;
import com.example.chalpuplatform.survey.service.SurveyQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys")
@Tag(name = "Survey Question", description = "서베이 질문 조회 API")
public class SurveyQuestionController {

    private final SurveyQuestionService surveyQuestionService;

    @GetMapping("/{surveyId}/questions")
    @Operation(
            summary = "서베이 질문 목록 조회",
            description = "특정 서베이에 포함된 모든 질문을 조회합니다. 사장님이 메뉴별로 활성화할 질문을 선택하기 위해 사용됩니다."
    )
    public ResponseEntity<ApiResponse<List<SurveyQuestionResponse>>> getQuestionsBySurveyId(
            @PathVariable("surveyId") Long surveyId) {

        List<SurveyQuestion> questions = surveyQuestionService.getQuestionsBySurveyId(surveyId);

        List<SurveyQuestionResponse> responses = questions.stream()
                .map(SurveyQuestionResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
