package com.example.chalpuplatform.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Value("${oauth2.redirect.failure-path:#{'/oauth2/failure'}}")
    private String failurePath;
    @Value("${oauth2.redirect.owner-domain:#{'owner.chalpu.com'}}")
    private String ownerDomain;
    @Value("${oauth2.redirect.customer-domain:#{'customer.chalpu.com'}}")
    private String customerDomain;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage = exception.getLocalizedMessage();
        log.error("OAuth2 인증 실패: {}", errorMessage, exception);

        // Origin/Referer 헤더로 클라이언트 도메인 결정
        String clientDomain = determineClientDomain(request);
        String redirectDomain = determineRedirectDomain(clientDomain);

        // 에러 메시지 인코딩
        String encodedMessage = URLEncoder.encode("OAuth 인증에 실패했습니다: " + errorMessage, StandardCharsets.UTF_8);

        // UriComponentsBuilder를 사용하여 일관된 방식으로 리다이렉트 URL 생성
        String targetUrl = UriComponentsBuilder.fromHttpUrl("https://" + redirectDomain)
                .path(failurePath)
                .queryParam("error", encodedMessage)
                .build().toUriString();

        log.info("OAuth2 인증 실패 리다이렉트: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Origin/Referer 헤더를 통해 클라이언트 도메인 결정
     */
    private String determineClientDomain(HttpServletRequest request) {
        // 1. Origin 헤더 확인 (CORS 요청시)
        String origin = request.getHeader("Origin");
        if (origin != null) {
            log.debug("Origin 헤더 감지: {}", origin);
            if (origin.contains(ownerDomain)) {
                log.info("Owner 도메인에서 OAuth2 실패 처리: {}", origin);
                return "owner";
            } else if (origin.contains(customerDomain)) {
                log.info("Customer 도메인에서 OAuth2 실패 처리: {}", origin);
                return "customer";
            }
        }

        // 2. Referer 헤더 확인 (일반 요청시)
        String referer = request.getHeader("Referer");
        if (referer != null) {
            log.debug("Referer 헤더 감지: {}", referer);
            if (referer.contains(ownerDomain)) {
                log.info("Owner 도메인에서 OAuth2 실패 처리 (Referer): {}", referer);
                return "owner";
            } else if (referer.contains(customerDomain)) {
                log.info("Customer 도메인에서 OAuth2 실패 처리 (Referer): {}", referer);
                return "customer";
            }
        }

        // 3. 기본값은 customer
        log.debug("도메인을 판단할 수 없음. 기본값 customer 사용 (Origin: {}, Referer: {})", origin, referer);
        return "customer";
    }

    /**
     * 리다이렉트할 도메인 결정
     */
    private String determineRedirectDomain(String clientDomain) {
        // 클라이언트 도메인이 owner이면 owner 도메인으로 리다이렉트
        if ("owner".equals(clientDomain)) {
            return ownerDomain;
        }

        // 기본값은 customer 도메인
        return customerDomain;
    }
}
