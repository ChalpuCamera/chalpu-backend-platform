package com.example.chalpuplatform.user.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.UserException;
import com.example.chalpuplatform.oauth.dto.UserInfoDTO;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.store.service.StoreService;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;
    private final StoreService storeService;

    @Transactional(readOnly = true)
    public UserInfoDTO getCurrentUser(UserDetailsImpl currentUser) {
        User user = userRepository.findByIdAndDeletedAtIsNull(currentUser.getId())
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        return UserInfoDTO.fromEntity(user);
    }
    
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_INVALID_CREDENTIALS));
    }

    /**
     * 이메일 중복 체크
     *
     * @param email 확인할 이메일
     * @return 중복되지 않은 경우 true, 중복된 경우 false
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    public void softDelete(Long userId) {
        User user = userRepository.findByIdWithDeleted(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        // 이미 삭제된 경우 추가 작업 없이 종료
        if (user.getDeletedAt() != null) {
            log.warn("event=AlreadyDeletedUserAttempt, userId={}", userId);
            return;
        }

        // 1. User가 OWNER인 Store들 조회 및 삭제
        List<UserStoreRole> ownerRoles = userStoreRoleRepository.findOwnerRolesByUserId(userId);
        for (UserStoreRole ownerRole : ownerRoles) {
            Long storeId = ownerRole.getStore().getId();
            storeService.deleteStore(storeId);
            log.info("event=OwnerStoreDeleted, userId={}, storeId={}", userId, storeId);
        }

        // 2. 남은 UserStoreRole들 소프트 딜리트
        userStoreRoleRepository.softDeleteByUserId(userId);

        log.info("event=UserSoftDeleted, userId={}", userId);

        // 3. User 자체 소프트 딜리트
        user.softDelete();
        userRepository.save(user);
    }

    public void activateUser(Long userId) {
        User user = userRepository.findByIdWithDeleted(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        user.activate();
        userStoreRoleRepository.activateByUserId(userId);
        userRepository.save(user);
    }
}