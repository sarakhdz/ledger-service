package io.github.sarakhdz.ledger.domain.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");

        if (currency.getDefaultFractionDigits() < 0) {
            throw new IllegalArgumentException("Not a monetary currency: " + currency.getCurrencyCode());
        }
        try {
            amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
        }catch (ArithmeticException e) {
            throw new IllegalArgumentException(currency.getCurrencyCode() + " allows at most "
                    + currency.getDefaultFractionDigits() + " decimals, got: " + amount);
        }
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money zero(String currencyCode) {
        return of("0", currencyCode);
    }
}