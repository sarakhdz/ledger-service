package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.InvalidAmountException;
import io.github.sarakhdz.ledger.domain.money.Money;

import java.util.Objects;

public final class ArrangedOverdraftPolicy implements WithdrawalPolicy {

    private final Money limit;

    public ArrangedOverdraftPolicy(Money limit) {
        Objects.requireNonNull(limit, "limit must not be null");
        if (limit.isNegative()) {
            throw new InvalidAmountException("Limit must not be negative, got: " + limit);
        }
        this.limit = limit;
    }

    @Override
    public boolean allows(Money currentBalance, Money amount) {
        return !currentBalance.subtract(amount).isLessThan(limit.negate());
    }
}
