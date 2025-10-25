package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.LinkType;
import com.example.chalpuplatform.store.domain.StoreLink;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 링크 응답")
public class StoreLinkResponse {

    @Schema(description = "링크 타입", example = "BAEMIN")
    private LinkType linkType;

    @Schema(description = "링크 라벨 (화면 표시용)", example = "배달의민족")
    private String label;

    @Schema(description = "URL", example = "https://baemin.com/store/12345")
    private String url;

    public static StoreLinkResponse from(StoreLink storeLink) {
        return StoreLinkResponse.builder()
                .linkType(storeLink.getLinkType())
                .label(storeLink.getLabel())
                .url(storeLink.getUrl())
                .build();
    }
}
