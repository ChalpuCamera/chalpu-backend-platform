package com.example.chalpuplatform.common.util;

import com.example.chalpuplatform.common.exception.CouponException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PhoneHashUtil 테스트")
class PhoneHashUtilTest {

    @Nested
    @DisplayName("전화번호 정규화 테스트")
    class NormalizePhoneTest {

        @ParameterizedTest
        @ValueSource(strings = {"010-1234-5678", "010 1234 5678", "01012345678"})
        @DisplayName("다양한 형식의 전화번호를 정규화한다")
        void normalizePhone_Success(String phone) {
            String normalized = PhoneHashUtil.normalizePhone(phone);

            assertThat(normalized).isEqualTo("01012345678");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "123", "0101234567", "011-1234-5678", "010-123-4567"})
        @DisplayName("유효하지 않은 전화번호는 예외를 발생시킨다")
        void normalizePhone_InvalidFormat_ThrowsException(String phone) {
            assertThatThrownBy(() -> PhoneHashUtil.normalizePhone(phone))
                    .isInstanceOf(CouponException.class);
        }

        @Test
        @DisplayName("null 전화번호는 예외를 발생시킨다")
        void normalizePhone_Null_ThrowsException() {
            assertThatThrownBy(() -> PhoneHashUtil.normalizePhone(null))
                    .isInstanceOf(CouponException.class);
        }
    }

    @Nested
    @DisplayName("전화번호 해싱 테스트")
    class HashPhoneTest {

        @Test
        @DisplayName("정규화된 전화번호를 SHA-256으로 해싱한다")
        void hashPhone_Success() {
            String phone = "01012345678";
            String hash = PhoneHashUtil.hashPhone(phone);

            assertThat(hash).hasSize(64);
            assertThat(hash).matches("^[a-f0-9]{64}$");
        }

        @Test
        @DisplayName("같은 전화번호는 같은 해시값을 생성한다")
        void hashPhone_SameInput_SameHash() {
            String phone = "01012345678";

            String hash1 = PhoneHashUtil.hashPhone(phone);
            String hash2 = PhoneHashUtil.hashPhone(phone);

            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("다른 전화번호는 다른 해시값을 생성한다")
        void hashPhone_DifferentInput_DifferentHash() {
            String phone1 = "01012345678";
            String phone2 = "01087654321";

            String hash1 = PhoneHashUtil.hashPhone(phone1);
            String hash2 = PhoneHashUtil.hashPhone(phone2);

            assertThat(hash1).isNotEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("정규화 및 해싱 통합 테스트")
    class NormalizeAndHashTest {

        @Test
        @DisplayName("전화번호를 정규화하고 해싱한다")
        void normalizeAndHash_Success() {
            String phone = "010-1234-5678";
            String hash = PhoneHashUtil.normalizeAndHash(phone);

            assertThat(hash).hasSize(64);
            assertThat(hash).matches("^[a-f0-9]{64}$");
        }

        @Test
        @DisplayName("형식이 다른 같은 번호는 같은 해시를 생성한다")
        void normalizeAndHash_DifferentFormat_SameHash() {
            String phone1 = "010-1234-5678";
            String phone2 = "010 1234 5678";
            String phone3 = "01012345678";

            String hash1 = PhoneHashUtil.normalizeAndHash(phone1);
            String hash2 = PhoneHashUtil.normalizeAndHash(phone2);
            String hash3 = PhoneHashUtil.normalizeAndHash(phone3);

            assertThat(hash1).isEqualTo(hash2).isEqualTo(hash3);
        }
    }
}
