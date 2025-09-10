package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.Store;
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
@Schema(description = "매장 응답")
public class StoreResponse {
    
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;
    
    @Schema(description = "매장명", example = "맛있는 식당")
    private String storeName;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;
    
    @Schema(description = "생성 시간", example = "2024-01-15T09:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정 시간", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
    
    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
} 