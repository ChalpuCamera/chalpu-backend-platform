package com.example.chalpuplatform.photo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대표 사진 지정")
public class PhotoSetFeaturedRequest {
    @Schema(description = "대표 사진 ID", example = "1")
    private Long photoId;

    @Schema(description = "음식 ID", example = "1")
    private Long foodItemId;
}
