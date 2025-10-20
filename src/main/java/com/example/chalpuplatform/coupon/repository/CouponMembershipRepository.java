package com.example.chalpuplatform.coupon.repository;

import com.example.chalpuplatform.coupon.domain.CouponMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponMembershipRepository extends JpaRepository<CouponMembership, Long> {

    Optional<CouponMembership> findByStoreIdAndPhoneHash(Long storeId, String phoneHash);

    boolean existsByStoreIdAndPhoneHash(Long storeId, String phoneHash);
}
