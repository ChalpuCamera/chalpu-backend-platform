package com.example.chalpuplatform.coupon.repository;

import com.example.chalpuplatform.coupon.domain.CouponPinHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CouponPinHistoryRepository extends JpaRepository<CouponPinHistory, Long> {

    Optional<CouponPinHistory> findByStoreIdAndPinAndIsUsedFalse(Long storeId, String pin);

    Optional<CouponPinHistory> findByStoreIdAndPhoneHashAndPinAndCreatedAt(Long storeId, String phoneHash, String pin, LocalDateTime createdAt);

    @Query("SELECT p FROM CouponPinHistory p " +
           "WHERE p.storeId = :storeId " +
           "AND p.phoneHash = :phoneHash " +
           "AND p.pin = :pin " +
           "ORDER BY p.createdAt DESC")
    Optional<CouponPinHistory> findLatestByStoreIdAndPhoneHashAndPin(
            @Param("storeId") Long storeId,
            @Param("phoneHash") String phoneHash,
            @Param("pin") String pin,
            Pageable pageable
    );
}
