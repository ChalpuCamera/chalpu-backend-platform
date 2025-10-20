package com.example.chalpuplatform.coupon.repository;

import com.example.chalpuplatform.coupon.domain.CouponPinHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponPinHistoryRepository extends JpaRepository<CouponPinHistory, Long> {

    Optional<CouponPinHistory> findByStoreIdAndPinAndIsUsedFalse(Long storeId, String pin);
}
