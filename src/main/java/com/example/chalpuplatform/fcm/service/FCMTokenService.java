package com.example.chalpuplatform.fcm.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.UserException;
import com.example.chalpuplatform.fcm.domain.FCMToken;
import com.example.chalpuplatform.fcm.dto.FCMTokenRequest;
import com.example.chalpuplatform.fcm.repository.FCMTokenRepository;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FCMTokenService {

    private final FCMTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    public void registerOrUpdateToken(FCMTokenRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        Optional<FCMToken> existingToken = fcmTokenRepository
                .findByUserAndDeviceType(user, request.getDeviceType());

        if (existingToken.isPresent()) {
            FCMToken token = existingToken.get();
            token.updateToken(
                    request.getFcmToken(),
                    request.getDeviceType(),
                    request.getDeviceModel(),
                    request.getOsVersion(),
                    request.getAppVersion(),
                    request.getIsAllowed()
            );
            log.info("FCM token updated: userId={}, deviceType={}", user.getId(), request.getDeviceType());
        } else {
            FCMToken newToken = FCMToken.create(
                    user,
                    request.getFcmToken(),
                    request.getDeviceType(),
                    request.getDeviceModel(),
                    request.getOsVersion(),
                    request.getAppVersion(),
                    request.getIsAllowed()
            );
            fcmTokenRepository.save(newToken);
            log.info("FCM token registered: userId={}, deviceType={}", user.getId(), request.getDeviceType());
        }
    }

    public void deleteToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));

        fcmTokenRepository.deleteByUser(user);
        log.info("FCM token deleted: userId={}", userId);
    }
}
