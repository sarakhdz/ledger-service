package io.github.sarakhdz.ledger.domain.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void amountsWithDifferentScaleAreEqual() {
        assertThat(Money.of("10.0", "EUR")).isEqualTo(Money.of("10.00", "EUR"));
        assertThat(Money.of("10.0", "EUR").hashCode())
                .isEqualTo(Money.of("10.00", "EUR").hashCode());
    }

    @Test
    void rejectsMoreDecimalsThanTheCurrencyAllows() {
        assertThatThrownBy(() -> Money.of("10.001", "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zeroDecimalCurrenciesAcceptWholeAmountsOnly() {
        assertThat(Money.of("100", "JPY").amount()).isEqualTo(new BigDecimal("100"));
        assertThatThrownBy(() -> Money.of("100.5", "JPY"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsCurrenciesWithoutDefinedDecimals() {
        assertThatThrownBy(() -> Money.of("10", "XXX"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}