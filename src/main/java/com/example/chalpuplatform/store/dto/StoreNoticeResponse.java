package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.StoreNotice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "가게 공지사항 응답")
public class StoreNoticeResponse {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "가게 ID", example = "1")
    private Long storeId;

    @Schema(description = "공지사항 제목", example = "설 연휴 휴무 안내")
    private String title;

    @Schema(description = "공지사항 내용", example = "2024년 2월 9일부터 2월 12일까지 휴무입니다.")
    private String body;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedAt;

    public static StoreNoticeResponse from(StoreNotice storeNotice) {
        return StoreNoticeResponse.builder()
                .id(storeNotice.getId())
                .storeId(storeNotice.getStoreId())
                .title(storeNotice.getTitle())
                .body(storeNotice.getBody())
                .createdAt(storeNotice.getCreatedAt())
                .updatedAt(storeNotice.getUpdatedAt())
                .build();
    }
}
