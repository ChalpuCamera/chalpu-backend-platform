package com.example.chalpuplatform.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "인증 코드 교환 요청")
public class CodeExchangeRequest {

    @Schema(description = "인증 코드", example = "abc123-def456-ghi789")
    private String code;
}