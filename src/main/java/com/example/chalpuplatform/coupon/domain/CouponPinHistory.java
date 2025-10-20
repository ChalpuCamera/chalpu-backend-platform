package com.example.chalpuplatform.coupon.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.common.exception.CouponException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "coupon_pin_histories",
    indexes = @Index(name = "idx_store_pin", columnList = "store_id, pin")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponPinHistory extends BaseTimeEntity {

    private static final Integer PIN_EXPIRATION_MINUTES = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "phone_hash", length = 64)
    private String phoneHash;

    @Column(name = "pin", nullable = false, length = 2)
    private String pin;

    @Column(name = "stamps", nullable = false)
    private Integer stamps;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    public static CouponPinHistory create(Long storeId, String pin, Integer stamps) {
        return CouponPinHistory.builder()
                .storeId(storeId)
                .pin(pin)
                .stamps(stamps)
                .isUsed(false)
                .expiredAt(LocalDateTime.now().plusMinutes(PIN_EXPIRATION_MINUTES))
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

    public boolean isValid() {
        return !this.isUsed && !isExpired();
    }

    public void markAsUsed(String phoneHash) {
        if (this.isUsed) {
            throw new CouponException(ErrorMessage.COUPON_PIN_ALREADY_USED);
        }
        if (isExpired()) {
            throw new CouponException(ErrorMessage.COUPON_PIN_EXPIRED);
        }
        this.isUsed = true;
        this.phoneHash = phoneHash;
    }
}
