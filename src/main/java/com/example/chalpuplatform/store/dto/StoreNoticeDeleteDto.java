package com.example.chalpuplatform.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공지사항 벌크 삭제 요청")
public class StoreNoticeDeleteDto {

    @NotEmpty
    @Schema(description = "삭제할 공지사항 ID 목록", example = "[1, 2, 3]")
    private List<Long> deleteIds;
}
