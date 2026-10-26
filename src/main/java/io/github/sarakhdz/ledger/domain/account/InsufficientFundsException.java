package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;
import io.github.sarakhdz.ledger.domain.shared.DomainException;

public class InsufficientFundsException extends DomainException {

    public InsufficientFundsException(AccountId accountId, Money requested, Money balance) {
        super("INSUFFICIENT FUNDS",
                "Withdrawal of " + requested + "not allowed for account " + accountId.value()
                        + ", balance is " + balance);
    }
}
