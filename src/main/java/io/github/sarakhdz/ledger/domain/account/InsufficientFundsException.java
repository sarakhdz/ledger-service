package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;
import io.github.sarakhdz.ledger.domain.shared.DomainException;

public final class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(AccountId accountId, Money requested, Money balance) {
        super("INSUFFICIENT_FUNDS",
                "Withdrawal of " + requested + " not allowed for account " + accountId.value()
                        + ", balance is " + balance);
    }
}
