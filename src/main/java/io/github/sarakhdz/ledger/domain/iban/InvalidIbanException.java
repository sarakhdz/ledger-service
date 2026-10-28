package io.github.sarakhdz.ledger.domain.iban;

import io.github.sarakhdz.ledger.domain.shared.DomainException;

public final class InvalidIbanException extends DomainException {

    public InvalidIbanException(String reason) {
        super("INVALID_IBAN", "Invalid IBAN: " + reason);
    }
}
