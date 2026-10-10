package com.minipay.common.money;

/**
 * An amount of Vietnamese dong. VND has no minor unit in practice, so one unit of {@code amount}
 * is one dong and a {@code long} is exact (no rounding, no floating point).
 *
 * <p>The value may be negative: a balance delta or a system account in the ledger can go below
 * zero. Rules such as "a deposit must be positive" belong to the command that uses the money,
 * not to the money itself.
 *
 * <p>Arithmetic uses {@link Math#addExact} so an overflow throws instead of silently wrapping
 * around to a huge negative number.
 */
public record Money(long amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    public static Money vnd(long amount) {
        return new Money(amount);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(amount, other.amount));
    }

    public Money minus(Money other) {
        return new Money(Math.subtractExact(amount, other.amount));
    }

    public Money negate() {
        return new Money(Math.negateExact(amount));
    }

    public boolean isPositive() {
        return amount > 0;
    }

    public boolean isNegative() {
        return amount < 0;
    }

    public boolean isGreaterThanOrEqualTo(Money other) {
        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(amount, other.amount);
    }

    @Override
    public String toString() {
        return amount + " VND";
    }
}
