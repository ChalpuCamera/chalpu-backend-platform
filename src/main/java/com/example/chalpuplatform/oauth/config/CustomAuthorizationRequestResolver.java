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
        // URL 경로에서 provider와 userType 추출
        String path = request.getRequestURI();

        // /api/oauth2/authorization/kakao/customer 형식 처리
        if (path != null && path.contains("/oauth2/authorization/")) {
            String[] parts = path.split("/");
            if (parts.length >= 5) {
                // provider 추출 (예: kakao, naver, google, apple)
                String provider = parts[parts.length - 2];
                String userType = parts[parts.length - 1];

                // customer나 owner인 경우
                if ("customer".equals(userType) || "owner".equals(userType)) {
                    // 기본 경로로 OAuth2AuthorizationRequest 생성
                    OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, provider);
                    if (authorizationRequest != null) {
                        log.info("OAuth2 요청 처리 - Provider: {}, UserType: {}", provider, userType);
                        return customizeAuthorizationRequest(authorizationRequest, userType);
                    }
                }
            }
        }

        // 기본 처리 (이전 버전 호환성)
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        if (authorizationRequest != null) {
            String userType = determineUserTypeFromDomain(request);
            return customizeAuthorizationRequest(authorizationRequest, userType);
        }

        return null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        // URL 경로에서 userType 확인
        String path = request.getRequestURI();
        String userType = "customer"; // 기본값

        if (path != null) {
            if (path.endsWith("/owner")) {
                userType = "owner";
                // clientRegistrationId에서 /owner 제거
                if (clientRegistrationId != null && clientRegistrationId.endsWith("/owner")) {
                    clientRegistrationId = clientRegistrationId.replace("/owner", "");
                }
            } else if (path.endsWith("/customer")) {
                userType = "customer";
                // clientRegistrationId에서 /customer 제거
                if (clientRegistrationId != null && clientRegistrationId.endsWith("/customer")) {
                    clientRegistrationId = clientRegistrationId.replace("/customer", "");
                }
            }
        }

        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        if (authorizationRequest != null) {
            log.info("OAuth2 인증 요청 - Provider: {}, UserType: {}, Path: {}",
                    clientRegistrationId, userType, path);
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