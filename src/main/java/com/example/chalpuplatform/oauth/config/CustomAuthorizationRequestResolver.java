package com.example.chalpuplatform.oauth.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/api/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);

        if (authorizationRequest != null) {
            String userType = determineUserTypeFromDomain(request);
            return customizeAuthorizationRequest(authorizationRequest, userType);
        }

        return null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);

        if (authorizationRequest != null) {
            String userType = determineUserTypeFromDomain(request);
            log.debug("OAuth2 인증 요청 - Host: {}, UserType: {}, Provider: {}",
                    request.getServerName(), userType, clientRegistrationId);
            return customizeAuthorizationRequest(authorizationRequest, userType);
        }

        return null;
    }

    /**
     * URL 경로에서 사용자 타입 결정
     * /api/oauth2/authorization/kakao/customer -> customer
     * /api/oauth2/authorization/kakao/owner -> owner
     */
    private String determineUserTypeFromDomain(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        // URL 경로에서 userType 추출
        if (requestUri != null) {
            if (requestUri.contains("/owner")) {
                log.info("Owner 타입 감지 from URL: {}", requestUri);
                return "owner";
            } else if (requestUri.contains("/customer")) {
                log.info("Customer 타입 감지 from URL: {}", requestUri);
                return "customer";
            }
        }

        // 기본값은 customer
        log.info("URL에서 사용자 타입을 판단할 수 없음. 기본값 customer 사용: {}", requestUri);
        return "customer";
    }


    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, String userType) {

        if (authorizationRequest == null) {
            return null;
        }

        // state 파라미터에 user_type 정보 추가 (기존 state 보존)
        String currentState = authorizationRequest.getState();
        String newState = currentState + "_usertype:" + userType;

        log.info("OAuth2 요청 커스터마이징 - userType: {}, state: {}", userType, newState);

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .state(newState)
                .build();
    }
}