package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;

public final class NoOverdraftPolicy implements WithdrawalPolicy {

    @Override
    public boolean allows(Money currentBalance, Money amount) {
        return !(currentBalance.subtract(amount)).isNegative();
    }
}
