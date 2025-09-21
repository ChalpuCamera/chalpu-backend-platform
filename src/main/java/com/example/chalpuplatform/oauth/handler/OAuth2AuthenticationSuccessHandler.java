package com.example.chalpuplatform.oauth.handler;

import com.example.chalpuplatform.oauth.model.AuthCode;
import com.example.chalpuplatform.oauth.repository.AuthCodeRepository;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.user.domain.UserLoginHistory;
import com.example.chalpuplatform.user.repository.UserLoginHistoryRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AuthCodeRepository authCodeRepository;
    private final UserLoginHistoryRepository userLoginHistoryRepository;

    @Value("${oauth2.redirect.success-path:#{'/oauth2/success'}}")
    private String successPath;
    @Value("${oauth2.redirect.failure-path:#{'/oauth2/failure'}}")
    private String failurePath;
    @Value("${oauth2.redirect.owner-domain:owner.chalpu.com}")
    private String ownerDomain;
    @Value("${oauth2.redirect.customer-domain:customer.chalpu.com}")
    private String customerDomain;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 사용자 역할 가져오기
            String userRole = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("ROLE_CUSTOMER");

            // 임시 인증 코드 생성
            String authCode = java.util.UUID.randomUUID().toString();

            // AuthCode 엔티티 생성 및 저장
            AuthCode authCodeEntity = AuthCode.builder()
                    .code(authCode)
                    .userId(userDetails.getId())
                    .userEmail(userDetails.getEmail())
                    .userRole(userRole)
                    .build();
            authCodeRepository.save(authCodeEntity);

            // 로그인 이력 저장 (실패해도 무시)
            try {
                userLoginHistoryRepository.save(UserLoginHistory.
                        createLoginHistory(userDetails.getId()));
            } catch (Exception e) {
                log.error("로그인 이력 저장 실패: userId={}, error={}", userDetails.getId(), e.getMessage());
            }

            log.info("OAuth2 로그인 성공 및 인증 코드 생성: userId={}, email={}, code={}, role={}",
                    userDetails.getId(), userDetails.getEmail(), authCode, userRole);

            // 도메인 기반 리다이렉트 URL 결정
            String targetUrl = buildRedirectUrl(request, userRole, authCode, true);

            log.info("OAuth2 인증 성공 리다이렉트: {}", targetUrl);

            // 리다이렉트 수행
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생: {}", e.getMessage(), e);

            // 오류 발생 시 실패 URL로 리다이렉트
            String errorUrl = buildRedirectUrl(request, null,
                    URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8), false);

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    /**
     * 도메인 기반으로 리다이렉트 URL 생성
     */
    private String buildRedirectUrl(HttpServletRequest request, String userRole, String param, boolean isSuccess) {
        String redirectDomain = determineRedirectDomain(userRole);
        String path = isSuccess ? successPath : failurePath;
        String paramName = isSuccess ? "code" : "error";

        return UriComponentsBuilder.fromHttpUrl("https://" + redirectDomain)
                .path(path)
                .queryParam(paramName, param)
                .build().toUriString();
    }

    /**
     * 리다이렉트할 도메인 결정
     */
    private String determineRedirectDomain(String userRole) {
        // role이 OWNER인 경우 owner 도메인으로
        if ("ROLE_OWNER".equals(userRole)) {
            log.info("Owner 역할 감지 - owner 도메인으로 리다이렉트");
            return !ownerDomain.isEmpty() ? ownerDomain : "owner.chalpu.com";
        }

        // 기본값은 customer 도메인
        log.info("Customer 역할 감지 - customer 도메인으로 리다이렉트");
        return !customerDomain.isEmpty() ? customerDomain : "customer.chalpu.com";
    }
}