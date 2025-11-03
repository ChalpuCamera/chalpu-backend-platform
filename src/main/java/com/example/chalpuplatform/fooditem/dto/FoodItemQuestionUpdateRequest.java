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

    @Schema(description = "설문 ID", example = "1", required = true)
    private Long surveyId;

    @NotEmpty
    @Schema(description = "활성화할 JAR 속성 목록", example = "[\"SPICINESS\", \"SWEETNESS\", \"PORTION_SIZE\"]")
    private List<JARAttribute> jarAttributes;
}
