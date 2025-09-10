package com.example.chalpuplatform.user.controller;

import com.example.chalpuplatform.common.exception.AuthException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.security.jwt.UserDetailsImpl;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.dto.UserDto;
import com.example.chalpuplatform.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 정보 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "JWT 인증을 통해 현재 로그인한 사용자의 상세 정보를 조회합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        
        if (currentUser == null) {
            throw new AuthException(ErrorMessage.AUTH_UNAUTHORIZED);
        }

        // 서비스를 통해 사용자 정보 조회
        User user = userService.getUserById(currentUser.getId());
        UserDto userDto = new UserDto(user);
        
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회가 완료되었습니다.", userDto));
    }

    @Operation(
        summary = "사용자 정보 삭제",
        description = "사용자 정보를 삭제합니다.",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        userService.softDelete(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
