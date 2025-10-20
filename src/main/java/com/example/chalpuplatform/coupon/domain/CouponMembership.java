package com.example.chalpuplatform.coupon.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import com.example.chalpuplatform.common.exception.CouponException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "coupon_memberships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "phone_hash"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CouponMembership extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membership_id")
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "phone_hash", nullable = false, length = 64)
    private String phoneHash;

    @Column(name = "current_stamps", nullable = false)
    @Builder.Default
    private Integer currentStamps = 0;

    public static CouponMembership create(Long storeId, String phoneHash) {
        return CouponMembership.builder()
                .storeId(storeId)
                .phoneHash(phoneHash)
                .currentStamps(0)
                .build();
    }

    public void addStamps(Integer stamps) {
        if (stamps == null || stamps <= 0) {
            throw new IllegalArgumentException("스탬프 수는 0보다 커야 합니다");
        }
        this.currentStamps += stamps;
    }

    public boolean canRedeem(Integer requiredStamps) {
        return this.currentStamps >= requiredStamps;
    }

    public void redeem(Integer requiredStamps) {
        if (!canRedeem(requiredStamps)) {
            throw new CouponException(ErrorMessage.COUPON_INSUFFICIENT_STAMPS);
        }
        this.currentStamps -= requiredStamps;
    }
}
