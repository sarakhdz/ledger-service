package io.github.sarakhdz.ledger.domain.money;

import io.github.sarakhdz.ledger.domain.shared.DomainException;

public final class InvalidAmountException extends DomainException {

    public InvalidAmountException(String message) {
        super("INVALID_AMOUNT", message);
    }
}
