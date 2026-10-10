package com.minipay.common.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void addsAndSubtractsExactly() {
        Money balance = Money.vnd(100_000).plus(Money.vnd(50_000)).minus(Money.vnd(30_000));

        assertThat(balance).isEqualTo(Money.vnd(120_000));
    }

    @Test
    void throwsOnOverflowInsteadOfWrappingAround() {
        Money max = Money.vnd(Long.MAX_VALUE);

        assertThatThrownBy(() -> max.plus(Money.vnd(1))).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void recordsCompareByValue() {
        assertThat(Money.vnd(1_000)).isEqualTo(new Money(1_000));
        assertThat(Money.vnd(1_000).hashCode()).isEqualTo(new Money(1_000).hashCode());
    }

    /** Why Money is not a double, and why BigDecimal needs care. Run these and read the output. */
    @Nested
    @DisplayName("Why not double?")
    class WhyNotDouble {

        @Test
        void doubleCannotRepresentDecimalFractionsExactly() {
            double total = 0.1 + 0.2;

            assertThat(total).isNotEqualTo(0.3);
            assertThat(total).isEqualTo(0.30000000000000004);
        }

        @Test
        void doubleLosesPrecisionAboveTwoToThe53() {
            // A double has a 52-bit mantissa: above 2^53 not every integer is representable.
            double big = 9_007_199_254_740_992d; // 2^53

            assertThat(big + 1).isEqualTo(big);
        }

        @Test
        void bigDecimalEqualsComparesScaleToo() {
            BigDecimal a = new BigDecimal("1.0");
            BigDecimal b = new BigDecimal("1.00");

            assertThat(a.equals(b)).isFalse();
            assertThat(a.compareTo(b)).isZero();
        }

        @Test
        void bigDecimalFromDoubleKeepsTheBinaryError() {
            assertThat(new BigDecimal(0.1)).isNotEqualTo(new BigDecimal("0.1"));
            assertThat(BigDecimal.valueOf(0.1)).isEqualTo(new BigDecimal("0.1"));
        }
    }
}
