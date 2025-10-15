package com.example.chalpuplatform.landing.dto;

import com.example.chalpuplatform.landing.domain.ButtonType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ButtonLogRequest {

    @Schema(description = "버튼 타입",
            example = "START_FREE")
    private ButtonType buttonType;
}
