package com.example.chalpuplatform.oauth.mobile.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.mobile.dto.GoogleLoginRequest;
import com.example.chalpuplatform.oauth.mobile.dto.LoginResponse;
import com.example.chalpuplatform.oauth.mobile.service.MobileOAuthService;
import com.example.chalpuplatform.oauth.model.AuthProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "OAuth Mobile", description = "모바일 OAuth 로그인 API")
public class OAuthController {

    private final MobileOAuthService mobileOAuthService;

    @PostMapping("/google/login")
    @Operation(
            summary = "Google 모바일 로그인",
            description = "Google SDK로부터 받은 ID Token을 검증하고 JWT를 발급합니다. userType에 따라 ROLE이 결정됩니다."
    )
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestBody GoogleLoginRequest request) {
        log.info("Google mobile login request received: userType={}", request.getUserType());
        LoginResponse response = mobileOAuthService.processOAuthLogin(AuthProvider.GOOGLE, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}