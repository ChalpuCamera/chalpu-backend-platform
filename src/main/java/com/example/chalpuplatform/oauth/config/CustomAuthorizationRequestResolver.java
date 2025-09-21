package com.example.chalpuplatform.oauth.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    @Value("${oauth2.redirect.owner-domain}")
    private String ownerDomain;

    @Value("${oauth2.redirect.customer-domain}")
    private String customerDomain;

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
     * 도메인으로부터 사용자 타입 결정
     */
    private String determineUserTypeFromDomain(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            log.debug("Origin 헤더 감지: {}", origin);
            if (origin.contains(ownerDomain)) {
                log.info("Owner 도메인에서 OAuth2 요청: {}", origin);
                return "owner";
            } else if (origin.contains(customerDomain)) {
                log.info("Customer 도메인에서 OAuth2 요청: {}", origin);
                return "customer";
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            log.debug("Referer 헤더 감지: {}", referer);
            if (referer.contains(ownerDomain)) {
                log.info("Owner 도메인에서 OAuth2 요청 (Referer): {}", referer);
                return "owner";
            } else if (referer.contains(customerDomain)) {
                log.info("Customer 도메인에서 OAuth2 요청 (Referer): {}", referer);
                return "customer";
            }
        }

        // 3. 기본값은 customer
        log.debug("도메인을 판단할 수 없음. 기본값 customer 사용 (Origin: {}, Referer: {})", origin, referer);
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