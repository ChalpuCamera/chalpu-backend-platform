package com.example.chalpuplatform.user.repository;

import com.example.chalpuplatform.user.domain.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {
}
