package com.example.chalpuplatform.store.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.response.PageResponse;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.StoreRoleType;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.dto.MemberInviteRequest;
import com.example.chalpuplatform.store.dto.MemberResponse;
import com.example.chalpuplatform.store.dto.StoreResponse;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStoreRoleService {

    private final UserStoreRoleRepository userStoreRoleRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    /**
     * 사용자가 속한 매장 목록 조회 (페이지네이션)
     */
    public PageResponse<StoreResponse> getMyStores(Long userId, Pageable pageable) {
        Page<UserStoreRole> userStoreRolePage = userStoreRoleRepository.findByUserIdAndIsActiveTrueWithStoreOnly(userId, pageable);
        Page<StoreResponse> storeResponsePage = userStoreRolePage.map(usr -> StoreResponse.from(usr.getStore()));
        log.info("event=my_stores_retrieved, user_id={}, total_elements={}, total_pages={}, current_page={}",
                userId, userStoreRolePage.getTotalElements(), userStoreRolePage.getTotalPages(), userStoreRolePage.getNumber());
        return PageResponse.from(storeResponsePage);
    }

    /**
     * 특정 매장의 멤버 목록 조회
     */
    public List<MemberResponse> getStoreMembers(Long storeId, Long requestUserId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

        // 권한 검증: 해당 매장에 속한 사용자만 멤버 목록 조회 가능
        List<UserStoreRole> requestUserRoles = userStoreRoleRepository.findByUserIdAndIsActiveTrue(requestUserId);
        if (!canUserAccessStore(requestUserRoles, store)) {
            log.error("canUserAccessStore: {}, 해당 매장에 속하지 않습니다.", canUserAccessStore(requestUserRoles, store));
            throw new StoreException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        // 매장의 모든 멤버 조회 (활성 멤버만)
        List<UserStoreRole> storeMembers = userStoreRoleRepository.findByStoreId(storeId)
                .stream()
                .filter(UserStoreRole::getIsActive)
                .toList();

        log.info("storeMembers: {}", storeMembers);
        return storeMembers.stream()
                .map(MemberResponse::from)
                .toList();
    }

    /**
     * 매장 소유자 역할 생성 (사용자 ID와 매장 ID로)
     */
    @Transactional
    public void createOwnerRole(Long userId, Long storeId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.USER_NOT_FOUND));
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));
            
            createOwnerRole(user, store);
            
            log.info("event=owner_role_created, user_id={}, store_id={}", userId, storeId);
        } catch (Exception e) {
            log.error("event=owner_role_creation_failed, user_id={}, store_id={}, error_message={}",
                    userId, storeId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 매장 소유자 역할 생성 (매장 생성 시 사용)
     */
    public void createOwnerRole(User user, Store store) {
        UserStoreRole ownerRole = UserStoreRole.createOwner(user, store);
        userStoreRoleRepository.save(ownerRole);
    }

    /**
     * 매장에 멤버 초대
     */
    @Transactional
    public MemberResponse inviteMember(Long storeId, MemberInviteRequest memberRequest, Long inviterUserId) {
        try {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));
            User inviteUser = userRepository.findById(memberRequest.getUserId())
                    .orElseThrow(() -> new StoreException(ErrorMessage.USER_NOT_FOUND));

            // 권한 검증: 멤버 초대 권한이 있는지 확인
            List<UserStoreRole> inviterRoles = userStoreRoleRepository.findByUserIdAndIsActiveTrue(inviterUserId);
            if (!canInviteMembers(inviterRoles, store)) {
                throw new StoreException(ErrorMessage.STORE_ACCESS_DENIED);
            }

            // 이미 매장 구성원인지 확인
            userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(memberRequest.getUserId(), storeId)
                .ifPresent(existingRole -> {
                    throw new StoreException(ErrorMessage.STORE_MEMBER_ALREADY_EXISTS);
                });

            // 직원 역할 생성
            UserStoreRole newUserRole = UserStoreRole.createEmployee(inviteUser, store, memberRequest.getRoleType());
            UserStoreRole savedUserStoreRole = userStoreRoleRepository.save(newUserRole);
            
            log.info("event=member_invited, store_id={}, invited_user_id={}, inviter_user_id={}",
                    storeId, memberRequest.getUserId(), inviterUserId);
            
            return MemberResponse.from(savedUserStoreRole);
        } catch (Exception e) {
            log.error("event=member_invitation_failed, store_id={}, invited_user_id={}, inviter_user_id={}, error_message={}",
                    storeId, memberRequest.getUserId(), inviterUserId, e.getMessage(), e);
            throw e;
        }
    }


    /**
     * 특정 매장에서 사용자의 권한 확인
     */
    public boolean canUserAccessStore(Long userId, Long storeId) {
        // Store 엔티티 조회 없이 storeId만으로 권한 확인
        List<UserStoreRole> userRoles = userStoreRoleRepository.findByUserIdAndIsActiveTrueWithoutJoin(userId);
        return userRoles.stream()
                .filter(UserStoreRole::getIsActive)
                .anyMatch(role -> role.getStore().getId().equals(storeId));
    }

    /**
     * 특정 매장에서 사용자의 관리 권한 확인
     */
    public boolean canUserManageStore(Long userId, Long storeId) {
        // Store 엔티티 조회 없이 storeId만으로 권한 확인
        Optional<UserStoreRole> userRole = userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrueWithoutJoin(userId, storeId);
        return userRole
                .filter(UserStoreRole::getIsActive)
                .map(UserStoreRole::canManageStore)
                .orElse(false);
    }

    // === 내부 유틸리티 메서드들 ===

    /**
     * 사용자가 특정 매장에 접근할 수 있는지 확인
     */
    private boolean canUserAccessStore(List<UserStoreRole> userRoles, Store store) {
        return userRoles.stream()
                .filter(UserStoreRole::getIsActive)
                .anyMatch(role -> role.getStore().equals(store));
    }

    /**
     * 사용자가 특정 매장에 멤버를 초대할 수 있는지 확인
     */
    private boolean canInviteMembers(List<UserStoreRole> userRoles, Store store) {
        return getUserRoleInStore(userRoles, store)
                .map(UserStoreRole::canInviteMembers)
                .orElse(false);
    }

    /**
     * 사용자의 특정 매장에서의 역할 조회
     */
    private Optional<UserStoreRole> getUserRoleInStore(List<UserStoreRole> userRoles, Store store) {
        return userRoles.stream()
                .filter(UserStoreRole::getIsActive)
                .filter(role -> role.getStore().equals(store))
                .findFirst();
    }
} 