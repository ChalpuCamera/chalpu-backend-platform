package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.LinkType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 링크 생성/수정 요청")
public class StoreLinkRequest {
    @Schema(
        description = "링크 타입",
        example = "BAEMIN"
    )
    private LinkType linkType;

    @Schema(description = "커스텀 라벨", example = "전화 주문, 배민으로 주문하러 가기")
    private String customLabel;

    @Schema(description = "URL", example = "https://example.com", required = true)
    private String url;
}
