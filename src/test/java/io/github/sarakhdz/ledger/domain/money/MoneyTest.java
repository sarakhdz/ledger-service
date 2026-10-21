package io.github.sarakhdz.ledger.domain.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

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

    @Test
    void rejectsNullAmountAndCurrency() {
        Currency eur = Currency.getInstance("EUR");
        assertThatThrownBy(() -> new Money(null, eur))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("amount must not be null");
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("currency must not be null");
    }

    @Test
    void addSubtractAndNegateReturnNewValues() {
        Money ten = Money.of("10.00", "EUR");
        Money threeFifty = Money.of("3.50", "EUR");

        assertThat(ten.add(threeFifty)).isEqualTo(Money.of("13.50", "EUR"));
        assertThat(ten.subtract(threeFifty)).isEqualTo(Money.of("6.50", "EUR"));
        assertThat(threeFifty.negate()).isEqualTo(Money.of("-3.50", "EUR"));
        assertThat(ten).isEqualTo(Money.of("10.00", "EUR"));   // the original didn't change
    }

    @Test
    void rejectsArithmeticAcrossCurrencies() {
        Money eur = Money.of("10.00", "EUR");
        Money usd = Money.of("10.00", "USD");
        assertThatThrownBy(() -> eur.add(usd)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> eur.subtract(usd)).isInstanceOf(IllegalArgumentException.class);
    }
}