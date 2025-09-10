package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.StoreRoleType;
import com.example.chalpuplatform.store.domain.UserStoreRole;
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
@Schema(description = "멤버 응답")
public class MemberResponse {
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;
    
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String userEmail;
    
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;
    
    @Schema(description = "역할", example = "EMPLOYEE")
    private StoreRoleType roleType;
    
    @Schema(description = "가입 시간", example = "2024-01-15T09:30:00")
    private LocalDateTime joinedAt;
    
    public static MemberResponse from(UserStoreRole userStoreRole) {
        return MemberResponse.builder()
                .userId(userStoreRole.getUser() != null ? userStoreRole.getUser().getId() : null)
                .userName(userStoreRole.getUser() != null ? userStoreRole.getUser().getName() : null)
                .userEmail(userStoreRole.getUser() != null ? userStoreRole.getUser().getEmail() : null)
                .storeId(userStoreRole.getStore() != null ? userStoreRole.getStore().getId() : null)
                .roleType(userStoreRole.getRoleType())
                .joinedAt(userStoreRole.getCreatedAt())
                .build();
    }
} 