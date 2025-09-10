package com.example.chalpuplatform.oauth.dto;

import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "사용자 정보")
public class UserInfoDTO {
    
    @Schema(description = "사용자 ID", example = "1")
    private Long id;
    
    @Schema(description = "이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "이름", example = "홍길동")
    private String name;
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/profile.jpg")
    private String picture;
    
    @Schema(description = "인증 제공자", example = "GOOGLE", enumAsRef = true)
    private AuthProvider provider;

    public static UserInfoDTO fromEntity(User user) {
        return UserInfoDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .provider(user.getProvider())
                .build();
    }
}
