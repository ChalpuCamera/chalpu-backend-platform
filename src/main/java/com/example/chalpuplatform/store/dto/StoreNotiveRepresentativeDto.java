package com.example.chalpuplatform.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreNotiveRepresentativeDto {
    @Schema(description = "공지사항 ID",example = "1")
    private Long storeNoticeId;
}
