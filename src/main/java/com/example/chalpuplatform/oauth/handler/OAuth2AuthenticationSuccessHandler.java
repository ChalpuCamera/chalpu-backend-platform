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
        // Origin/Referer 헤더로 클라이언트 도메인 결정
        String clientDomain = determineClientDomain(request);
        String redirectDomain = determineRedirectDomain(clientDomain, userRole);
        String path = isSuccess ? successPath : failurePath;
        String paramName = isSuccess ? "code" : "error";

        return UriComponentsBuilder.fromHttpUrl("https://" + redirectDomain)
                .path(path)
                .queryParam(paramName, param)
                .build().toUriString();
    }

    /**
     * Origin/Referer 헤더를 통해 클라이언트 도메인 결정
     */
    private String determineClientDomain(HttpServletRequest request) {
        log.info("도메인 판별 시작 - ownerDomain: {}, customerDomain: {}", ownerDomain, customerDomain);

        // 1. Origin 헤더 확인 (CORS 요청시)
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isEmpty()) {
            log.info("Origin 헤더 감지: {}", origin);
            if (origin.contains("owner")) {
                log.info("Owner 도메인에서 OAuth2 성공 처리 (Origin contains 'owner'): {}", origin);
                return "owner";
            } else if (origin.contains("customer")) {
                log.info("Customer 도메인에서 OAuth2 성공 처리 (Origin contains 'customer'): {}", origin);
                return "customer";
            }
        }

        // 2. Referer 헤더 확인 (일반 요청시)
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            log.info("Referer 헤더 감지: {}", referer);
            if (referer.contains("owner")) {
                log.info("Owner 도메인에서 OAuth2 성공 처리 (Referer contains 'owner'): {}", referer);
                return "owner";
            } else if (referer.contains("customer")) {
                log.info("Customer 도메인에서 OAuth2 성공 처리 (Referer contains 'customer'): {}", referer);
                return "customer";
            }
        }

        // 3. 기본값은 customer
        log.info("도메인을 판단할 수 없음. 기본값 customer 사용 (Origin: {}, Referer: {})", origin, referer);
        return "customer";
    }

    /**
     * 리다이렉트할 도메인 결정
     */
    private String determineRedirectDomain(String clientDomain, String userRole) {
        // 1. 클라이언트 도메인이 owner이거나 role이 OWNER인 경우
        if ("owner".equals(clientDomain) || "ROLE_OWNER".equals(userRole)) {
            return !ownerDomain.isEmpty() ? ownerDomain : "owner.chalpu.com";
        }

        // 2. 기본값은 customer 도메인
        return !customerDomain.isEmpty() ? customerDomain : "customer.chalpu.com";
    }
}