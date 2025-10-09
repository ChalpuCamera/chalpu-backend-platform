package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 ID 응답")
public class StoreIdResponse {

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장명", example = "우리집냉면")
    private String storeName;

    public static StoreIdResponse from(Store store) {
        return StoreIdResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .build();
    }
}
