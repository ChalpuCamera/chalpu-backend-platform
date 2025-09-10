package com.example.chalpuplatform.oauth.security.oauth2.policy;

import com.example.chalpuplatform.common.exception.AuthException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 탈퇴 사용자 재가입 정책 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivatedUserPolicy {
    
    private static final int REJOIN_WAITING_DAYS = 30;
    private final UserService userService;
    
    /**
     * 탈퇴 사용자 재활성화 가능 여부 확인 및 처리
     */
    public void checkAndHandleDeactivatedUser(User user) {
        if (!isDeactivated(user)) {
            return;
        }
        
        if (canReactivate(user)) {
            reactivateUser(user);
        } else {
            throwRejoinUnavailableException();
        }
    }
    
    /**
     * 사용자가 탈퇴 상태인지 확인
     */
    private boolean isDeactivated(User user) {
        return user.getDeletedAt() != null;
    }
    
    /**
     * 재활성화 가능한지 확인 (탈퇴 후 30일 경과)
     */
    private boolean canReactivate(User user) {
        LocalDateTime reactivationDate = user.getDeletedAt().plusDays(REJOIN_WAITING_DAYS);
        return reactivationDate.isBefore(LocalDateTime.now());
    }
    
    /**
     * 사용자 계정 재활성화
     */
    private void reactivateUser(User user) {
        userService.activateUser(user.getId());
        log.info("탈퇴 후 {}일이 경과하여 계정을 복구합니다: {}", 
                REJOIN_WAITING_DAYS, user.getEmail());
    }
    
    /**
     * 재가입 불가 예외 발생
     */
    private void throwRejoinUnavailableException() {
        log.warn("탈퇴 후 {}일이 경과하지 않아 재가입 불가", REJOIN_WAITING_DAYS);
        throw new AuthException(ErrorMessage.USER_DEACTIVATED_REJOIN_UNAVAILABLE);
    }
}