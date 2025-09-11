package com.example.chalpuplatform.oauth.jwt;

import com.example.chalpuplatform.common.exception.AuthException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.oauth.dto.TokenDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMinutes;
    private final long refreshTokenValidityInDays;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token.validity-in-minutes:720000}") long accessTokenValidityInMinutes,
            @Value("${jwt.refresh-token.validity-in-days:14}") long refreshTokenValidityInDays) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMinutes = accessTokenValidityInMinutes;
        this.refreshTokenValidityInDays = refreshTokenValidityInDays;
    }

    /**
     * Access Token과 Refresh Token을 함께 생성하여 TokenDTO로 반환
     */
    public TokenDTO generateTokens(Long userId, String email, String role) {
        String accessToken = generateAccessToken(userId, email, role);
        String refreshToken = generateRefreshToken(userId);

        log.info("토큰 생성 완료: 사용자 ID = {}", userId);

        return new TokenDTO(accessToken, refreshToken);
    }

    // Access Token 생성 (15분)
    public String generateAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenValidityInMinutes, ChronoUnit.MINUTES);

        return Jwts.builder().subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성 (14일)
    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenValidityInDays, ChronoUnit.DAYS);

        return Jwts.builder().subject(userId.toString())
                .claim("type", "refresh").issuedAt(Date.from(now)).expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 테스트용 Access Token 생성 (10년)
     */
    public String generateTestAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(3650, ChronoUnit.DAYS); // 10 years

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(getClaimsFromToken(token).getSubject());
    }

    // 토큰에서 이메일 추출 (Access Token만)
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    // 토큰에서 role 추출 (Access Token만)
    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    // 토큰 타입 확인
    public String getTokenType(String token) {
        return getClaimsFromToken(token).get("type", String.class);
    }

    // 토큰 유효성 검증
    public void validateToken(String token) {
        try {
            getClaimsFromToken(token);
        } catch (ExpiredJwtException e) {
            log.error("event=jwt_token_expired, token={}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new AuthException(ErrorMessage.JWT_EXPIRED);
        } catch (MalformedJwtException e) {
            log.error("event=jwt_token_malformed, error_message={}", e.getMessage());
            throw new AuthException(ErrorMessage.JWT_MALFORMED);
        } catch (UnsupportedJwtException e) {
            log.error("event=jwt_token_unsupported, error_message={}", e.getMessage());
            throw new AuthException(ErrorMessage.JWT_UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            log.error("event=jwt_token_claims_empty, error_message={}", e.getMessage());
            throw new AuthException(ErrorMessage.JWT_CLAIMS_EMPTY);
        } catch (JwtException e) {
            log.error("event=jwt_token_invalid_signature, error_message={}", e.getMessage());
            throw new AuthException(ErrorMessage.JWT_INVALID_SIGNATURE);
        }
    }

    // Access Token인지 확인
    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    // Refresh Token인지 확인
    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    // 토큰에서 Claims 추출
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
