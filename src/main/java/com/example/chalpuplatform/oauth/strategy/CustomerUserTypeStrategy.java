package com.example.chalpuplatform.oauth.strategy;

import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.user.domain.Role;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.domain.UserProfile;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객(Customer) 타입 사용자 처리 전략
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerUserTypeStrategy implements UserTypeStrategy {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public OAuth2User processUser(User user, OAuth2User oAuth2User) {
        user.setRole(Role.ROLE_CUSTOMER);
        
        // UserProfile이 없으면 생성
        if (user.getUserProfile() == null) {
            UserProfile profile = UserProfile.create(user);
            user.setUserProfile(profile);
            log.info("고객 사용자 프로필 생성: {}", user.getEmail());
        }
        
        userRepository.save(user);
        log.info("고객 사용자로 처리 완료: {}", user.getEmail());
        
        return UserDetailsImpl.build(user, oAuth2User.getAttributes());
    }
    
    @Override
    public String getUserType() {
        return "customer";
    }
}