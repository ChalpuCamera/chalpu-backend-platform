package com.example.chalpuplatform.oauth.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void generateTestAccessTokenForOwner() {
        // Given
        Long userId = 4L;
        String email = "owner@test.com";
        String role = "ROLE_OWNER";
        
        // When
        String accessToken = jwtTokenProvider.generateTestAccessToken(userId, email, role);
        
        // Then
        System.out.println("=== Test Access Token (10 years) ===");
        System.out.println("User ID: " + userId);
        System.out.println("Email: " + email);
        System.out.println("Role: " + role);
        System.out.println("Token: " + accessToken);
        System.out.println("=====================================");
    }
}