package com.example.chalpuplatform.coupon.domain;

import com.example.chalpuplatform.common.exception.CouponException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CouponMembership 도메인 테스트")
class CouponMembershipTest {

    private CouponMembership membership;

    @BeforeEach
    void setUp() {
        membership = CouponMembership.create(1L, "hashedPhone123");
    }

    @Nested
    @DisplayName("멤버십 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("멤버십을 생성한다")
        void create_Success() {
            CouponMembership newMembership = CouponMembership.create(1L, "hashedPhone");

            assertThat(newMembership.getStoreId()).isEqualTo(1L);
            assertThat(newMembership.getPhoneHash()).isEqualTo("hashedPhone");
            assertThat(newMembership.getCurrentStamps()).isZero();
        }
    }

    @Nested
    @DisplayName("스탬프 추가 테스트")
    class AddStampsTest {

        @Test
        @DisplayName("스탬프를 추가한다")
        void addStamps_Success() {
            membership.addStamps(5);

            assertThat(membership.getCurrentStamps()).isEqualTo(5);
        }

        @Test
        @DisplayName("스탬프를 누적해서 추가한다")
        void addStamps_Accumulate() {
            membership.addStamps(3);
            membership.addStamps(2);
            membership.addStamps(5);

            assertThat(membership.getCurrentStamps()).isEqualTo(10);
        }

        @Test
        @DisplayName("0 이하의 스탬프는 추가할 수 없다")
        void addStamps_Zero_ThrowsException() {
            assertThatThrownBy(() -> membership.addStamps(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("0보다 커야");
        }

        @Test
        @DisplayName("음수 스탬프는 추가할 수 없다")
        void addStamps_Negative_ThrowsException() {
            assertThatThrownBy(() -> membership.addStamps(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 스탬프는 추가할 수 없다")
        void addStamps_Null_ThrowsException() {
            assertThatThrownBy(() -> membership.addStamps(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 가능 여부 테스트")
    class CanRedeemTest {

        @Test
        @DisplayName("스탬프가 부족하면 사용할 수 없다")
        void canRedeem_InsufficientStamps_ReturnsFalse() {
            membership.addStamps(9);

            assertThat(membership.canRedeem(10)).isFalse();
        }

        @Test
        @DisplayName("스탬프가 충분하면 사용할 수 있다")
        void canRedeem_SufficientStamps_ReturnsTrue() {
            membership.addStamps(10);

            assertThat(membership.canRedeem(10)).isTrue();
        }

        @Test
        @DisplayName("스탬프가 필요량보다 많아도 사용할 수 있다")
        void canRedeem_MoreThanRequired_ReturnsTrue() {
            membership.addStamps(15);

            assertThat(membership.canRedeem(10)).isTrue();
        }

        @Test
        @DisplayName("매장별 설정값에 따라 사용 가능 여부가 결정된다")
        void canRedeem_DifferentRequiredStamps() {
            membership.addStamps(12);

            assertThat(membership.canRedeem(10)).isTrue();
            assertThat(membership.canRedeem(15)).isFalse();
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 테스트")
    class RedeemTest {

        @Test
        @DisplayName("쿠폰을 사용하면 스탬프가 차감된다")
        void redeem_Success() {
            membership.addStamps(15);

            membership.redeem(10);

            assertThat(membership.getCurrentStamps()).isEqualTo(5);
        }

        @Test
        @DisplayName("스탬프가 부족하면 쿠폰을 사용할 수 없다")
        void redeem_InsufficientStamps_ThrowsException() {
            membership.addStamps(9);

            assertThatThrownBy(() -> membership.redeem(10))
                    .isInstanceOf(CouponException.class);
        }

        @Test
        @DisplayName("쿠폰을 여러 번 사용할 수 있다")
        void redeem_Multiple() {
            membership.addStamps(25);

            membership.redeem(10);
            assertThat(membership.getCurrentStamps()).isEqualTo(15);

            membership.redeem(10);
            assertThat(membership.getCurrentStamps()).isEqualTo(5);
        }

        @Test
        @DisplayName("매장별 설정값에 따라 차감된다")
        void redeem_DifferentRequiredStamps() {
            membership.addStamps(20);

            membership.redeem(15);

            assertThat(membership.getCurrentStamps()).isEqualTo(5);
        }
    }
}
