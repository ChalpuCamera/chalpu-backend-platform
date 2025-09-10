package com.example.chalpuplatform.oauth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private static final String CUSTOMER_BASE_URI = "/api/oauth2/authorization/customer";
    private static final String OWNER_BASE_URI = "/api/oauth2/authorization/owner";

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/api/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        String path = request.getRequestURI();
        OAuth2AuthorizationRequest authorizationRequest = null;
        
        // customer 경로 처리
        if (path.startsWith(CUSTOMER_BASE_URI)) {
            String newPath = path.replace("/customer", "");
            authorizationRequest = resolveRequest(request, newPath);
            if (authorizationRequest != null) {
                return customizeAuthorizationRequest(authorizationRequest, "customer");
            }
        }
        
        // owner 경로 처리
        if (path.startsWith(OWNER_BASE_URI)) {
            String newPath = path.replace("/owner", "");
            authorizationRequest = resolveRequest(request, newPath);
            if (authorizationRequest != null) {
                return customizeAuthorizationRequest(authorizationRequest, "owner");
            }
        }
        
        // 기본 경로 처리
        if (defaultResolver != null) {
            return defaultResolver.resolve(request);
        }
        
        return null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        if (defaultResolver != null) {
            OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
            String path = request.getRequestURI();
            
            if (path.contains("/customer/")) {
                return customizeAuthorizationRequest(authorizationRequest, "customer");
            } else if (path.contains("/owner/")) {
                return customizeAuthorizationRequest(authorizationRequest, "owner");
            }
            
            return authorizationRequest;
        }
        return null;
    }

    private OAuth2AuthorizationRequest resolveRequest(HttpServletRequest request, String path) {
        if (defaultResolver != null) {
            HttpServletRequest modifiedRequest = new HttpServletRequestWrapper(request, path);
            return defaultResolver.resolve(modifiedRequest);
        }
        return null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, String userType) {
        
        if (authorizationRequest == null) {
            return null;
        }
        
        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
        additionalParameters.put("user_type", userType);
        
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
    
    // HttpServletRequest Wrapper 클래스
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final String requestURI;
        
        public HttpServletRequestWrapper(HttpServletRequest request, String requestURI) {
            super(request);
            this.requestURI = requestURI;
        }
        
        @Override
        public String getRequestURI() {
            return requestURI;
        }
    }
}