package com.example.chalpuplatform.user.domain;

public enum Role {
    ROLE_CUSTOMER,  // 일반 고객
    ROLE_OWNER,     // 매장 사장님
    ROLE_USER,      // 기본 사용자 (deprecated - 하위 호환성)
    ROLE_ADMIN      // 시스템 관리자
}
