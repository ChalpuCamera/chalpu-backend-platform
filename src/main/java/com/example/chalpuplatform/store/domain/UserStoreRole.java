package com.example.chalpuplatform.store.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.user.domain.User;
import jakarta.persistence.*;
import lombok.*;


@NamedEntityGraph(
    name = "UserStoreRole.withUserAndStore",
    attributeNodes = {
        @NamedAttributeNode("user"),
        @NamedAttributeNode("store")
    }
)
@Entity
@Table(name = "user_store_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserStoreRole extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreRoleType roleType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // 정적 팩토리 메서드
    public static UserStoreRole createOwner(User user, Store store) {
        return UserStoreRole.builder()
                .user(user)
                .store(store)
                .roleType(StoreRoleType.OWNER)
                .isActive(true)
                .build();
    }
    
    public static UserStoreRole createEmployee(User user, Store store, StoreRoleType roleType) {
        if (roleType == StoreRoleType.OWNER) {
            throw new IllegalArgumentException("직원 생성 시 OWNER 역할은 사용할 수 없습니다");
        }
        return UserStoreRole.builder()
                .user(user)
                .store(store)
                .roleType(roleType)
                .isActive(true)
                .build();
    }

    // 비즈니스 로직 메서드
    public boolean canManageStore() {
        return isActive && roleType.canManageStore();
    }

    public boolean canInviteMembers() {
        return isActive && roleType.canInviteMembers();
    }

    public boolean canModifyMenu() {
        return isActive && roleType.canModifyMenu();
    }

    public boolean isOwner() {
        return roleType == StoreRoleType.OWNER || roleType == StoreRoleType.CO_OWNER;
    }

    public boolean hasHigherAuthorityThan(UserStoreRole other) {
        return this.roleType.hasHigherAuthorityThan(other.roleType);
    }

    public Store getAssociatedStore() {
        return this.store;
    }                                                                                       

    // 상태 변경 메서드
    public void softDelete() {
        this.isActive = false;
    }

    public void changeRole(StoreRoleType newRoleType) {
        if (!this.isActive) {
            throw new IllegalStateException("비활성화된 역할은 변경할 수 없습니다");
        }
        this.roleType = newRoleType;
    }
} 