package com.example.chalpuplatform.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "리프레쉬 토큰 정보 (토큰 갱신용)")
public class RefreshTokenDTO {

    @Schema(description = "리프레쉬 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
}

