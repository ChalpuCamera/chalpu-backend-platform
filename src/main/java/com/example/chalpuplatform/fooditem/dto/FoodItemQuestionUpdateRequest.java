package com.example.chalpuplatform.fooditem.dto;

import com.example.chalpuplatform.jar.domain.JARAttribute;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메뉴별 활성화 질문 설정 요청")
public class FoodItemQuestionUpdateRequest {

    @NotEmpty
    @Schema(description = "활성화할 질문 ID 목록", example = "[1, 2, 3, 4, 5]")
    private List<Long> questionIds;
}
