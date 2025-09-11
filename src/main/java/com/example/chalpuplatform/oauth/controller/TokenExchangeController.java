package com.example.chalpuplatform.oauth.controller;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.dto.CodeExchangeRequest;
import com.example.chalpuplatform.oauth.dto.TokenExchangeResponse;
import com.example.chalpuplatform.oauth.model.AuthCode;
import com.example.chalpuplatform.oauth.repository.AuthCodeRepository;
import com.example.chalpuplatform.oauth.jwt.JwtTokenProvider;
import com.example.chalpuplatform.oauth.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Token Exchange", description = "토큰 교환 API")
public class TokenExchangeController {

    private final AuthCodeRepository authCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/token/exchange")
    @Operation(summary = "인증 코드로 토큰 교환", description = "OAuth2 인증 후 받은 코드로 액세스 토큰과 리프레시 토큰을 받습니다")
    @Transactional
    public ResponseEntity<ApiResponse<TokenExchangeResponse>> exchangeCode(
            @RequestBody CodeExchangeRequest request,
            HttpServletResponse response) {

        log.info("토큰 교환 요청: code={}", request.getCode());

        // 1. 인증 코드 검증
        AuthCode authCode = authCodeRepository.findByCode(request.getCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다"));

        // 2. 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                authCode.getUserId(),
                authCode.getUserEmail(),
                authCode.getUserRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(authCode.getUserId());

        // 3. Refresh Token DB 저장
        refreshTokenService.saveRefreshToken(refreshToken, authCode.getUserId());

        // 4. Refresh Token을 HttpOnly 쿠키로 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/api/auth");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshTokenCookie);

        // 6. 사용된 인증 코드 삭제 (일회용)
        authCodeRepository.delete(authCode);

        log.info("토큰 교환 성공: userId={}, role={}", authCode.getUserId(), authCode.getUserRole());

        // 7. Access Token과 메타데이터 응답
        TokenExchangeResponse tokenResponse = TokenExchangeResponse.builder()
                .accessToken(accessToken)
                .role(authCode.getUserRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success("토큰 발급이 완료되었습니다", tokenResponse));
    }
}