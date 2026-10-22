package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;

public interface WithdrawalPolicy {
    boolean allows(Money currentBalance, Money amount);
}
