package com.example.chalpuplatform.oauth.service;

import com.example.chalpuplatform.common.exception.OAuth2AuthenticationProcessingException;
import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.oauth.strategy.UserTypeStrategyFactory;
import com.example.chalpuplatform.oauth.provider.OAuth2UserInfo;
import com.example.chalpuplatform.oauth.provider.OAuth2UserInfoFactory;
import com.example.chalpuplatform.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

/**
 * OAuth2 사용자 처리 조정자
 * 전체 OAuth2 인증 프로세스를 조정하는 메인 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2UserProcessor {
    
    private final OAuth2UserValidator validator;
    private final OAuth2UserResolver userResolver;
    private final UserTypeStrategyFactory strategyFactory;
    
    /**
     * OAuth2 사용자 처리 메인 메서드
     */
    @Transactional
    public OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        try {
            // 1. OAuth2 정보 추출
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                    registrationId, oAuth2User.getAttributes());
            
            // 2. 유효성 검증
            validator.validate(userInfo);
            
            // 3. 사용자 조회 또는 생성
            AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());
            User user = userResolver.resolveUser(userInfo, provider);
            
            // 4. 사용자 타입에 따른 처리
            String userType = extractUserType(userRequest);
            return strategyFactory.getStrategy(userType)
                    .processUser(user, oAuth2User);
            
        } catch (OAuth2AuthenticationProcessingException ex) {
            log.error("OAuth2 인증 처리 실패: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("OAuth2 인증 처리 중 예상치 못한 오류: {}", ex.getMessage(), ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }
    
    /**
     * OAuth2UserRequest에서 user_type 추출
     * 우선순위: 1) URL 경로, 2) state 파라미터, 3) 기본값
     */
    private String extractUserType(OAuth2UserRequest userRequest) {
        try {
            // 현재 HTTP 요청에서 state 파라미터 추출
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // 1. URL 경로에서 userType 확인 (우선순위 최상)
                String requestUri = request.getRequestURI();
                if (requestUri != null) {
                    if (requestUri.contains("/owner")) {
                        log.info("URL 경로에서 owner 타입 감지: {}", requestUri);
                        return "owner";
                    } else if (requestUri.contains("/customer")) {
                        log.info("URL 경로에서 customer 타입 감지: {}", requestUri);
                        return "customer";
                    }
                }

                // 2. state 파라미터에서 확인 (백업용)
                String state = request.getParameter("state");
                if (state != null && state.contains("_usertype:")) {
                    // state에서 user_type 추출
                    String userType = state.substring(state.indexOf("_usertype:") + 10);
                    log.info("State에서 user_type 추출: state={}, userType={}", state, userType);
                    return userType;
                }
            }
        } catch (Exception e) {
            log.error("user_type 추출 실패: {}", e.getMessage());
        }

        // 3. 기본값
        log.info("user_type을 찾을 수 없음, 기본값 customer 사용");
        return "customer";
    }
}