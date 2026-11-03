package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.shared.DomainException;

public final class AccountNotFoundException extends DomainException {

    public AccountNotFoundException(AccountId id) {
        super("ACCOUNT_NOT_FOUND", "No account with id " + id.value());
    }
}
