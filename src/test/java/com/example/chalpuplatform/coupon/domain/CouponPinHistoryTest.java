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
        pinHistory = CouponPinHistory.create(1L, "47", 2);
    }

    @Nested
    @DisplayName("PIN 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("PIN을 생성한다")
        void create_Success() {
            CouponPinHistory newPin = CouponPinHistory.create(1L, "99", 5);

            assertThat(newPin.getStoreId()).isEqualTo(1L);
            assertThat(newPin.getPin()).isEqualTo("99");
            assertThat(newPin.getStamps()).isEqualTo(5);
            assertThat(newPin.getIsUsed()).isFalse();
            assertThat(newPin.getPhoneHash()).isNull();
        }

        @Test
        @DisplayName("PIN 만료 시간은 생성 시점으로부터 3분 후다")
        void create_ExpirationTime() {
            LocalDateTime before = LocalDateTime.now().plusMinutes(3).minusSeconds(1);
            CouponPinHistory newPin = CouponPinHistory.create(1L, "47", 2);
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
            pinHistory.markAsUsed("hashedPhone");

            assertThat(pinHistory.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("PIN 사용 처리 테스트")
    class MarkAsUsedTest {

        @Test
        @DisplayName("PIN을 사용 처리한다")
        void markAsUsed_Success() {
            String phoneHash = "hashedPhone123";

            pinHistory.markAsUsed(phoneHash);

            assertThat(pinHistory.getIsUsed()).isTrue();
            assertThat(pinHistory.getPhoneHash()).isEqualTo(phoneHash);
        }

        @Test
        @DisplayName("이미 사용된 PIN은 다시 사용할 수 없다")
        void markAsUsed_AlreadyUsed_ThrowsException() {
            pinHistory.markAsUsed("hashedPhone1");

            assertThatThrownBy(() -> pinHistory.markAsUsed("hashedPhone2"))
                    .isInstanceOf(CouponException.class);
        }
    }
}
