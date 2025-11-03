package com.example.chalpuplatform.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "가게 공지사항 수정 요청")
public class UpdateStoreNoticeRequest {

    @NotBlank
    @Schema(description = "공지사항 제목", example = "설 연휴 휴무 안내")
    private String title;

    @NotBlank
    @Schema(description = "공지사항 내용", example = "2024년 2월 9일부터 2월 12일까지 휴무입니다.")
    private String body;

    @Schema(description = "대표 공지사항 여부", example = "true")
    private Boolean isRepresentative;
}
