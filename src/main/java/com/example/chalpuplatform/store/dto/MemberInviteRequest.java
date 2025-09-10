package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.StoreRoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "멤버 초대 요청")
public class MemberInviteRequest {
    
    @Schema(description = "초대할 사용자 ID", example = "1", required = true)
    private Long userId;
    
    @Schema(description = "역할", example = "EMPLOYEE", required = true)
    private StoreRoleType roleType;

    @Schema(description = "소유 비율", example = "50")
    private BigDecimal ownershipPercentage;
} 