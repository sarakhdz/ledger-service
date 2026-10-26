package io.github.sarakhdz.ledger.domain.money;

import io.github.sarakhdz.ledger.domain.shared.DomainException;

import java.util.Currency;

public final class CurrencyMismatchException extends DomainException {

    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("CURRENCY_MISMATCH",
                "Currency mismatch: " + expected.getCurrencyCode() + " and " + actual.getCurrencyCode());
    }
}
