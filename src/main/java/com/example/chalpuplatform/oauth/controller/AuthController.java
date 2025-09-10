package com.example.chalpuplatform.oauth.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.dto.AccessTokenDTO;
import com.example.chalpuplatform.oauth.dto.RefreshTokenDTO;
import com.example.chalpuplatform.oauth.security.jwt.UserDetailsImpl;
import com.example.chalpuplatform.oauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AccessTokenDTO>> refresh(@RequestBody RefreshTokenDTO refreshToken) {
        AccessTokenDTO newAccessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("토큰 갱신이 완료되었습니다.", newAccessToken));
    }

    @Operation(summary = "로그아웃 처리", description = "현재 로그인한 사용자의 리프레쉬 토큰을 삭제합니다")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl currentUser){
        // 현재 사용자의 토큰 삭제 처리
        authService.logout(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}