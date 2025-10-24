package com.example.chalpuplatform.coupon.service;

import com.example.chalpuplatform.common.exception.CouponException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.common.util.PhoneHashUtil;
import com.example.chalpuplatform.coupon.domain.CouponMembership;
import com.example.chalpuplatform.coupon.domain.CouponPinHistory;
import com.example.chalpuplatform.coupon.dto.*;
import com.example.chalpuplatform.coupon.repository.CouponMembershipRepository;
import com.example.chalpuplatform.coupon.repository.CouponPinHistoryRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponService {

    private final CouponMembershipRepository membershipRepository;
    private final CouponPinHistoryRepository pinHistoryRepository;
    private final UserStoreRoleRepository userStoreRoleRepository;
    private final StoreRepository storeRepository;

    private static final Integer MIN_PIN = 10;
    private static final Integer MAX_PIN = 99;

    @Transactional(readOnly = true)
    public CouponMembershipResponse getMembership(Long storeId, String phone) {
        String phoneHash = PhoneHashUtil.normalizeAndHash(phone);

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

        return membershipRepository.findByStoreIdAndPhoneHash(storeId, phoneHash)
                .map(membership -> CouponMembershipResponse.from(membership, store.getRequiredStampsForCoupon()))
                .orElse(CouponMembershipResponse.empty(store.getRequiredStampsForCoupon()));
    }

    public CouponGeneratePinResponse generatePinForCustomer(CouponGeneratePinRequest request) {
        String phoneHash = PhoneHashUtil.normalizeAndHash(request.getPhone());

        storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

        String pin = generatePin();

        CouponPinHistory pinHistory = CouponPinHistory.createForCustomer(
                request.getStoreId(),
                pin,
                phoneHash
        );

        pinHistoryRepository.save(pinHistory);

        log.info("고객 PIN 생성 완료: storeId={}, pin={}", request.getStoreId(), pin);

        return CouponGeneratePinResponse.builder()
                .pin(pin)
                .expiredAt(pinHistory.getExpiredAt())
                .build();
    }

    public CouponRedeemResponse redeemCoupon(CouponRedeemRequest request) {
        String phoneHash = PhoneHashUtil.normalizeAndHash(request.getPhone());

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreException(ErrorMessage.STORE_NOT_FOUND));

        CouponMembership membership = membershipRepository
                .findByStoreIdAndPhoneHash(request.getStoreId(), phoneHash)
                .orElseThrow(() -> new CouponException(ErrorMessage.COUPON_MEMBERSHIP_NOT_FOUND));

        membership.redeem(store.getRequiredStampsForCoupon());
        membershipRepository.save(membership);

        log.info("쿠폰 사용 완료: storeId={}, requiredStamps={}, remainingStamps={}",
                request.getStoreId(), store.getRequiredStampsForCoupon(), membership.getCurrentStamps());

        return CouponRedeemResponse.builder()
                .success(true)
                .currentStamps(membership.getCurrentStamps())
                .build();
    }

    public CouponEarnStampsByOwnerResponse earnStampsByOwner(Long userId, CouponEarnStampsByOwnerRequest request) {
        boolean hasPermission = userStoreRoleRepository
                .findByUserIdAndStoreIdAndIsActiveTrue(userId, request.getStoreId())
                .isPresent();

        log.info("userID: {} and storeid: {}",userId,request.getStoreId());

        if (!hasPermission) {
            throw new StoreException(ErrorMessage.STORE_ACCESS_DENIED);
        }

        CouponPinHistory pinHistory = pinHistoryRepository
                .findByStoreIdAndPinAndIsUsedFalse(request.getStoreId(), request.getPin())
                .orElseThrow(() -> new CouponException(ErrorMessage.COUPON_PIN_NOT_FOUND));

        if (!pinHistory.isValid()) {
            if (pinHistory.isExpired()) {
                throw new CouponException(ErrorMessage.COUPON_PIN_EXPIRED);
            }
            throw new CouponException(ErrorMessage.COUPON_PIN_ALREADY_USED);
        }

        pinHistory.confirmStamps(request.getStamps());
        pinHistoryRepository.save(pinHistory);

        CouponMembership membership = membershipRepository
                .findByStoreIdAndPhoneHash(request.getStoreId(), pinHistory.getPhoneHash())
                .orElseGet(() -> {
                    CouponMembership newMembership = CouponMembership.create(request.getStoreId(), pinHistory.getPhoneHash());
                    return membershipRepository.save(newMembership);
                });

        membership.addStamps(request.getStamps());
        membershipRepository.save(membership);

        log.info("사장님 스탬프 적립 완료: userId={}, storeId={}, pin={}, stamps={}, currentStamps={}",
                userId, request.getStoreId(), request.getPin(), request.getStamps(), membership.getCurrentStamps());

        return CouponEarnStampsByOwnerResponse.builder()
                .success(true)
                .currentStamps(membership.getCurrentStamps())
                .addedStamps(request.getStamps())
                .build();
    }

    @Transactional(readOnly = true)
    public PinStatusResponse checkPinStatus(Long storeId, String phone, String pin) {
        String phoneHash = PhoneHashUtil.normalizeAndHash(phone);

        CouponPinHistory pinHistory = pinHistoryRepository
                .findLatestByStoreIdAndPhoneHashAndPin(storeId, phoneHash, pin)
                .orElseThrow(() -> new CouponException(ErrorMessage.COUPON_PIN_NOT_FOUND));

        log.debug("PIN 상태 조회: storeId={}, pin={}, isUsed={}, isExpired={}",
                storeId, pin, pinHistory.getIsUsed(), pinHistory.isExpired());

        return PinStatusResponse.from(pinHistory);
    }

    private String generatePin() {
        Random random = new Random();
        int pin = random.nextInt(MAX_PIN - MIN_PIN + 1) + MIN_PIN;
        return String.format("%02d", pin);
    }
}
