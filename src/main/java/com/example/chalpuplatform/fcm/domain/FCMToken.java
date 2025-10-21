package com.example.chalpuplatform.fcm.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FCMToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "os_version", length = 50)
    private String osVersion;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Builder.Default
    @Column(name = "is_allowed", nullable = false)
    private Boolean isAllowed = true;

    @Column(name = "last_used_at")
    private java.time.LocalDateTime lastUsedAt;

    public static FCMToken create(User user, String fcmToken, DeviceType deviceType,
                                   String deviceModel, String osVersion,
                                   String appVersion, Boolean isAllowed) {
        return FCMToken.builder()
                .user(user)
                .fcmToken(fcmToken)
                .deviceType(deviceType)
                .deviceModel(deviceModel)
                .osVersion(osVersion)
                .appVersion(appVersion)
                .isAllowed(isAllowed != null ? isAllowed : true)
                .build();
    }

    public void updateToken(String fcmToken, DeviceType deviceType, String deviceModel,
                            String osVersion, String appVersion, Boolean isAllowed) {
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        if (isAllowed != null) {
            this.isAllowed = isAllowed;
        }
    }

    public void deactivate() {
        this.isAllowed = false;
    }

    public void updateLastUsed() {
        this.lastUsedAt = java.time.LocalDateTime.now();
    }
}
