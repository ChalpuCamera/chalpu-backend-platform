package com.example.chalpuplatform.coupon.service;

import com.example.chalpuplatform.common.exception.CouponException;
import com.example.chalpuplatform.common.exception.StoreException;
import com.example.chalpuplatform.coupon.domain.CouponMembership;
import com.example.chalpuplatform.coupon.domain.CouponPinHistory;
import com.example.chalpuplatform.coupon.dto.*;
import com.example.chalpuplatform.coupon.repository.CouponMembershipRepository;
import com.example.chalpuplatform.coupon.repository.CouponPinHistoryRepository;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.UserStoreRole;
import com.example.chalpuplatform.store.repository.StoreRepository;
import com.example.chalpuplatform.store.repository.UserStoreRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService 테스트")
class CouponServiceTest {

    @Mock
    private CouponMembershipRepository membershipRepository;

    @Mock
    private CouponPinHistoryRepository pinHistoryRepository;

    @Mock
    private UserStoreRoleRepository userStoreRoleRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private CouponService couponService;

    private Store store;
    private CouponMembership membership;
    private CouponPinHistory pinHistory;

    @BeforeEach
    void setUp() {
        store = Store.builder()
                .id(1L)
                .storeName("테스트 매장")
                .requiredStampsForCoupon(10)
                .build();

        membership = CouponMembership.builder()
                .id(1L)
                .storeId(1L)
                .phoneHash("hashedPhone")
                .currentStamps(5)
                .build();

        pinHistory = CouponPinHistory.builder()
                .id(1L)
                .storeId(1L)
                .pin("47")
                .stamps(2)
                .isUsed(false)
                .expiredAt(LocalDateTime.now().plusMinutes(3))
                .build();
    }

    @Nested
    @DisplayName("멤버십 조회 테스트")
    class GetMembershipTest {

        @Test
        @DisplayName("존재하는 멤버십을 조회한다")
        void getMembership_Exists_ReturnsResponse() {
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(membershipRepository.findByStoreIdAndPhoneHash(anyLong(), anyString()))
                    .willReturn(Optional.of(membership));

            CouponMembershipResponse response = couponService.getMembership(1L, "010-1234-5678");

            assertThat(response.getCurrentStamps()).isEqualTo(5);
            assertThat(response.getRequiredStamps()).isEqualTo(10);
            assertThat(response.getCanRedeem()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 멤버십은 빈 응답을 반환한다")
        void getMembership_NotExists_ReturnsEmpty() {
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(membershipRepository.findByStoreIdAndPhoneHash(anyLong(), anyString()))
                    .willReturn(Optional.empty());

            CouponMembershipResponse response = couponService.getMembership(1L, "010-1234-5678");

            assertThat(response.getCurrentStamps()).isZero();
            assertThat(response.getRequiredStamps()).isEqualTo(10);
            assertThat(response.getCanRedeem()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 매장은 예외를 발생시킨다")
        void getMembership_StoreNotFound_ThrowsException() {
            given(storeRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.getMembership(1L, "010-1234-5678"))
                    .isInstanceOf(StoreException.class);
        }
    }

    @Nested
    @DisplayName("스탬프 적립 테스트")
    class EarnStampsTest {

        @Test
        @DisplayName("유효한 PIN으로 스탬프를 적립한다")
        void earnStamps_ValidPin_Success() {
            CouponEarnRequest request = new CouponEarnRequest(1L, "010-1234-5678", "47");

            given(pinHistoryRepository.findByStoreIdAndPinAndIsUsedFalse(1L, "47"))
                    .willReturn(Optional.of(pinHistory));
            given(membershipRepository.findByStoreIdAndPhoneHash(anyLong(), anyString()))
                    .willReturn(Optional.of(membership));
            given(membershipRepository.save(any())).willReturn(membership);
            given(pinHistoryRepository.save(any())).willReturn(pinHistory);

            CouponEarnResponse response = couponService.earnStamps(request);

            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getCurrentStamps()).isEqualTo(7);
            verify(pinHistoryRepository).save(any());
            verify(membershipRepository).save(any());
        }

        @Test
        @DisplayName("멤버십이 없으면 새로 생성한다")
        void earnStamps_NewMembership_CreatesNew() {
            CouponEarnRequest request = new CouponEarnRequest(1L, "010-1234-5678", "47");

            given(pinHistoryRepository.findByStoreIdAndPinAndIsUsedFalse(1L, "47"))
                    .willReturn(Optional.of(pinHistory));
            given(membershipRepository.findByStoreIdAndPhoneHash(anyLong(), anyString()))
                    .willReturn(Optional.empty());
            given(membershipRepository.save(any())).willReturn(membership);
            given(pinHistoryRepository.save(any())).willReturn(pinHistory);

            CouponEarnResponse response = couponService.earnStamps(request);

            assertThat(response.getSuccess()).isTrue();
            verify(membershipRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 PIN은 예외를 발생시킨다")
        void earnStamps_InvalidPin_ThrowsException() {
            CouponEarnRequest request = new CouponEarnRequest(1L, "010-1234-5678", "99");

            given(pinHistoryRepository.findByStoreIdAndPinAndIsUsedFalse(1L, "99"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.earnStamps(request))
                    .isInstanceOf(CouponException.class);
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 테스트")
    class RedeemCouponTest {

        @Test
        @DisplayName("쿠폰을 사용한다")
        void redeemCoupon_Success() {
            CouponRedeemRequest request = new CouponRedeemRequest(1L, "010-1234-5678");
            CouponMembership membershipWith10Stamps = CouponMembership.builder()
                    .id(1L)
                    .storeId(1L)
                    .phoneHash("hashedPhone")
                    .currentStamps(10)
                    .build();

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(membershipRepository.findByStoreIdAndPhoneHash(anyLong(), anyString()))
                    .willReturn(Optional.of(membershipWith10Stamps));
            given(membershipRepository.save(any())).willReturn(membershipWith10Stamps);

            CouponRedeemResponse response = couponService.redeemCoupon(request);

            assertThat(response.getSuccess()).isTrue();
            assertThat(response.getCurrentStamps()).isZero();
            verify(membershipRepository).save(any());
        }

        @Test
        @DisplayName("멤버십이 없으면 예외를 발생시킨다")
        void redeemCoupon_NoMembership_ThrowsException() {
            CouponRedeemRequest request = new CouponRedeemRequest(1L, "010-1234-5678");

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(membershipRepository.findByStoreIdAndPhoneHash(anyLong(), anyString()))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.redeemCoupon(request))
                    .isInstanceOf(CouponException.class);
        }

        @Test
        @DisplayName("스탬프가 부족하면 예외를 발생시킨다")
        void redeemCoupon_InsufficientStamps_ThrowsException() {
            CouponRedeemRequest request = new CouponRedeemRequest(1L, "010-1234-5678");

            given(storeRepository.findById(1L)).willReturn(Optional.of(store));
            given(membershipRepository.findByStoreIdAndPhoneHash(anyLong(), anyString()))
                    .willReturn(Optional.of(membership));

            assertThatThrownBy(() -> couponService.redeemCoupon(request))
                    .isInstanceOf(CouponException.class);
        }
    }

    @Nested
    @DisplayName("PIN 발급 테스트")
    class IssuePinTest {

        @Test
        @DisplayName("권한이 있으면 PIN을 발급한다")
        void issuePin_WithPermission_Success() {
            CouponIssuePinRequest request = new CouponIssuePinRequest(1L, 2);

            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.of(mock(UserStoreRole.class)));
            given(pinHistoryRepository.save(any())).willReturn(pinHistory);

            CouponIssuePinResponse response = couponService.issuePin(1L, request);

            assertThat(response.getPin()).hasSize(2);
            assertThat(response.getStamps()).isEqualTo(2);
            assertThat(response.getExpiredAt()).isNotNull();
            verify(pinHistoryRepository).save(any());
        }

        @Test
        @DisplayName("권한이 없으면 예외를 발생시킨다")
        void issuePin_NoPermission_ThrowsException() {
            CouponIssuePinRequest request = new CouponIssuePinRequest(1L, 2);

            given(userStoreRoleRepository.findByUserIdAndStoreIdAndIsActiveTrue(1L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.issuePin(1L, request))
                    .isInstanceOf(StoreException.class);
        }
    }
}
