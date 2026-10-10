package com.minipay.playground;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.minipay.common.money.Money;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * Executable notes on Java 17-21 language features, using MiniPay-flavoured examples.
 * Nothing here is production code; it is a place to experiment.
 */
class ModernJavaFeaturesTest {

    // --- Sealed interface: the compiler knows the complete list of implementations ---

    sealed interface WalletCommand permits Deposit, Transfer, QrPayment, Refund {}

    // --- Records: immutable data carriers with equals/hashCode/toString generated ---

    record Deposit(String walletId, Money amount) implements WalletCommand {
        // Compact constructor: validation runs before the fields are assigned.
        Deposit {
            Objects.requireNonNull(walletId, "walletId");
            if (!amount.isPositive()) {
                throw new IllegalArgumentException("deposit amount must be positive: " + amount);
            }
        }
    }

    record Transfer(String fromWalletId, String toWalletId, Money amount) implements WalletCommand {
        Transfer {
            if (fromWalletId.equals(toWalletId)) {
                throw new IllegalArgumentException("cannot transfer to the same wallet");
            }
        }
    }

    record QrPayment(String walletId, String merchantId, Money amount) implements WalletCommand {}

    record Refund(String originalTransactionId, Money amount) implements WalletCommand {}

    static final Money LARGE_TRANSFER_THRESHOLD = Money.vnd(50_000_000);
    static final Money LARGE_TRANSFER_FEE = Money.vnd(5_500);

    /**
     * Switch expression + pattern matching over a sealed type. There is no {@code default}
     * branch: if someone adds a new WalletCommand, this method stops compiling until it handles
     * the new case. That is the real value of sealed types.
     */
    static Money fee(WalletCommand command) {
        return switch (command) {
            case Deposit d -> Money.ZERO;
            // Record pattern (deconstruction) + guard. Order matters: the guarded case comes first.
            case Transfer(var from, var to, Money amount)
            when amount.isGreaterThanOrEqualTo(LARGE_TRANSFER_THRESHOLD) -> LARGE_TRANSFER_FEE;
            case Transfer t -> Money.ZERO;
            case QrPayment p -> Money.ZERO; // the merchant pays the fee (MDR), not the user
            case Refund r -> Money.ZERO;
        };
    }

    static String describe(Object event) {
        // Pattern matching for instanceof: test and bind in one step, no cast.
        if (event instanceof Transfer t && t.amount().isPositive()) {
            return "transfer of " + t.amount();
        }
        return "something else";
    }

    @Test
    void recordsAreValueObjects() {
        var a = new Deposit("w-1", Money.vnd(10_000));
        var b = new Deposit("w-1", Money.vnd(10_000));

        assertThat(a).isEqualTo(b);
        assertThat(a.toString()).isEqualTo("Deposit[walletId=w-1, amount=10000 VND]");
    }

    @Test
    void compactConstructorRejectsInvalidState() {
        assertThatThrownBy(() -> new Deposit("w-1", Money.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Transfer("w-1", "w-1", Money.vnd(1))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void switchOverSealedTypeComputesFee() {
        List<WalletCommand> commands = List.of(
                new Deposit("w-1", Money.vnd(100_000)),
                new Transfer("w-1", "w-2", Money.vnd(200_000)),
                new Transfer("w-1", "w-2", Money.vnd(60_000_000)),
                new QrPayment("w-1", "m-1", Money.vnd(45_000)),
                new Refund("tx-1", Money.vnd(45_000)));

        Money totalFee = commands.stream().map(ModernJavaFeaturesTest::fee).reduce(Money.ZERO, Money::plus);

        assertThat(totalFee).isEqualTo(LARGE_TRANSFER_FEE);
    }

    @Test
    void instanceofPatternBindsVariable() {
        assertThat(describe(new Transfer("w-1", "w-2", Money.vnd(1)))).isEqualTo("transfer of 1 VND");
        assertThat(describe("hello")).isEqualTo("something else");
    }

    @Test
    void textBlocksKeepSqlReadable() {
        String sql = """
                SELECT id, balance
                FROM wallet
                WHERE user_id = ?
                """;

        assertThat(sql).startsWith("SELECT").endsWith("?\n");
    }
}
