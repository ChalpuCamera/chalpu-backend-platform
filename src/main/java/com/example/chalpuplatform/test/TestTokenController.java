package com.example.chalpuplatform.test;

import com.example.chalpuplatform.common.response.ApiResponse;
import com.example.chalpuplatform.oauth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class TestTokenController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/token")
    public ApiResponse<String> generateTestToken(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "ROLE_CUSTOMER") String role) {

        String token = jwtTokenProvider.generateTestAccessToken(userId, email, role);

        log.info("테스트 토큰 생성: userId={}, email={}, role={}", userId, email, role);
        log.info("생성된 토큰: {}", token);

        return ApiResponse.success(token);
    }
}