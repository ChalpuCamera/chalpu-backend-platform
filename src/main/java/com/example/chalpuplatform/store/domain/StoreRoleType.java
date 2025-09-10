package com.example.chalpuplatform.store.domain;

import lombok.Getter;

@Getter
public enum StoreRoleType {
    OWNER(100),
    CO_OWNER(90),
    MANAGER(70),
    STAFF(30);
    
    private final int authorityLevel;
    
    StoreRoleType(int authorityLevel) {
        this.authorityLevel = authorityLevel;
    }
    
    public boolean canManageStore() {
        return this.authorityLevel >= 70;
    }
    
    public boolean canInviteMembers() {
        return this.authorityLevel >= 70;
    }
    
    public boolean canModifyMenu() {
        return this.authorityLevel >= 70;
    }
    
    public boolean hasHigherAuthorityThan(StoreRoleType other) {
        return this.authorityLevel > other.authorityLevel;
    }

    public static StoreRoleType fromString(String roleType) {
        for (StoreRoleType type : StoreRoleType.values()) {
            if (type.name().equalsIgnoreCase(roleType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid role type: " + roleType);
    }
} 