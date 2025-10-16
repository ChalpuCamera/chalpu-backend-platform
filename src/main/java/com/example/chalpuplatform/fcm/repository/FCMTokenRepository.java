package com.example.chalpuplatform.fcm.repository;

import com.example.chalpuplatform.fcm.domain.DeviceType;
import com.example.chalpuplatform.fcm.domain.FCMToken;
import com.example.chalpuplatform.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

    Optional<FCMToken> findByUserAndDeviceType(User user, DeviceType deviceType);

    Optional<FCMToken> findByUser(User user);

    void deleteByUser(User user);
}
