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
        } catch (ArithmeticException e) {
            throw new InvalidAmountException(currency.getCurrencyCode() + " allows at most "
                    + currency.getDefaultFractionDigits() + " decimals, got: " + amount);
        }
    }

    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money zero(String currencyCode) {
        return of("0", currencyCode);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount().add(other.amount()), currency());
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount().subtract(other.amount()), currency());
    }

    public Money negate() {
        return new Money(amount().negate(), currency());
    }

    public boolean isPositive() {
        return amount().signum() > 0;
    }

    public boolean isNegative() {
        return amount().signum() < 0;
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return amount().compareTo(other.amount()) > 0;
    }

    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return amount().compareTo(other.amount()) < 0;
    }

    @Override
    public String toString() {
        return currency().getCurrencyCode() + " " + amount().toPlainString();
    }

    private void requireSameCurrency(Money other) {
        if (!currency().equals(other.currency())) {
            throw new CurrencyMismatchException(currency(), other.currency());
        }
    }
}
