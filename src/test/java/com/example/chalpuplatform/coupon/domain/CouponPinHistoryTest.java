package com.example.chalpuplatform.coupon.domain;

import com.example.chalpuplatform.common.exception.CouponException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CouponPinHistory 도메인 테스트")
class CouponPinHistoryTest {

    private CouponPinHistory pinHistory;

    @BeforeEach
    void setUp() {
        pinHistory = CouponPinHistory.createForCustomer(1L, "47", "hashedPhone");
    }

    @Nested
    @DisplayName("PIN 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("고객이 PIN을 생성한다")
        void createForCustomer_Success() {
            CouponPinHistory newPin = CouponPinHistory.createForCustomer(1L, "99", "hashedPhone123");

            assertThat(newPin.getStoreId()).isEqualTo(1L);
            assertThat(newPin.getPin()).isEqualTo("99");
            assertThat(newPin.getStamps()).isNull();
            assertThat(newPin.getIsUsed()).isFalse();
            assertThat(newPin.getPhoneHash()).isEqualTo("hashedPhone123");
        }

        @Test
        @DisplayName("PIN 만료 시간은 생성 시점으로부터 3분 후다")
        void createForCustomer_ExpirationTime() {
            LocalDateTime before = LocalDateTime.now().plusMinutes(3).minusSeconds(1);
            CouponPinHistory newPin = CouponPinHistory.createForCustomer(1L, "47", "hashedPhone");
            LocalDateTime after = LocalDateTime.now().plusMinutes(3).plusSeconds(1);

            assertThat(newPin.getExpiredAt()).isBetween(before, after);
        }
    }

    @Nested
    @DisplayName("PIN 유효성 검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("생성 직후 PIN은 유효하다")
        void isValid_JustCreated_ReturnsTrue() {
            assertThat(pinHistory.isValid()).isTrue();
        }

        @Test
        @DisplayName("생성 직후 PIN은 만료되지 않았다")
        void isExpired_JustCreated_ReturnsFalse() {
            assertThat(pinHistory.isExpired()).isFalse();
        }

        @Test
        @DisplayName("사용된 PIN은 유효하지 않다")
        void isValid_Used_ReturnsFalse() {
            pinHistory.confirmStamps(2);

            assertThat(pinHistory.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("스탬프 확정 테스트")
    class ConfirmStampsTest {

        @Test
        @DisplayName("사장님이 스탬프를 확정한다")
        void confirmStamps_Success() {
            pinHistory.confirmStamps(2);

            assertThat(pinHistory.getIsUsed()).isTrue();
            assertThat(pinHistory.getStamps()).isEqualTo(2);
        }

        @Test
        @DisplayName("이미 확정된 PIN은 다시 확정할 수 없다")
        void confirmStamps_AlreadyUsed_ThrowsException() {
            pinHistory.confirmStamps(2);

            assertThatThrownBy(() -> pinHistory.confirmStamps(3))
                    .isInstanceOf(CouponException.class);
        }

        @Test
        @DisplayName("0 이하의 스탬프는 확정할 수 없다")
        void confirmStamps_Zero_ThrowsException() {
            assertThatThrownBy(() -> pinHistory.confirmStamps(0))
                    .isInstanceOf(CouponException.class);
        }

        @Test
        @DisplayName("null 스탬프는 확정할 수 없다")
        void confirmStamps_Null_ThrowsException() {
            assertThatThrownBy(() -> pinHistory.confirmStamps(null))
                    .isInstanceOf(CouponException.class);
        }
    }
}
