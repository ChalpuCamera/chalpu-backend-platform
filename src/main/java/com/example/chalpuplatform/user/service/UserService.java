package com.example.chalpuplatform.user.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.UserException;
import com.example.chalpuplatform.oauth.dto.UserInfoDTO;
import com.example.chalpuplatform.oauth.jwt.UserDetailsImpl;
import com.example.chalpuplatform.photo.repository.PhotoRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;

    public UserInfoDTO getCurrentUser(UserDetailsImpl currentUser) {
        User user = userRepository.findByIdAndDeletedAtIsNull(currentUser.getId())
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        return UserInfoDTO.fromEntity(user);
    }
    
    public User getUserById(Long id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
    }

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
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Transactional
    public void softDelete(Long userId) {
        User user = userRepository.findByIdWithDeleted(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
        
        // 이미 삭제된 경우 추가 작업 없이 종료
        if (user.getDeletedAt() != null) {
            log.warn("event=AlreadyDeletedUserAttempt, userId={}", userId);
            return;
        }
        // 2. 연관된 UserStoreRole들 소프트 딜리트  
        userStoreRoleRepository.softDeleteByUserId(userId);

        log.info("event=UserSoftDeleted, userId={}", userId);

        // 4. User 자체 소프트 딜리트
        user.softDelete();
        userRepository.save(user); // 변경된 상태를 DB에 반영
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findByIdWithDeleted(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        user.activate();
        userStoreRoleRepository.activateByUserId(userId);
        userRepository.save(user);
    }
}