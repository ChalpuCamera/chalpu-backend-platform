package com.example.chalpuplatform.landing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContactInquiryRequest {

    @Schema(description = "문의 내용", example = "서비스에 대해 문의드립니다.")
    private String content;
}
