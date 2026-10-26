package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Account {

    private final AccountId id;
    private final ProductType productType;
    private final WithdrawalPolicy withdrawalPolicy;
    private final Clock clock;
    private Money balance;
    private final List<LedgerEntry> entries = new ArrayList<>();

    private Account(AccountId id, ProductType productType, WithdrawalPolicy withdrawalPolicy, Clock clock) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.balance = Money.zero("EUR");
        this.productType = productType;
        this.withdrawalPolicy = withdrawalPolicy;
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public static Account openSavingsAccount(Clock clock) {
        return openSavingsAccount(AccountId.newId(), clock);
    }

    public static Account openSavingsAccount(AccountId id, Clock clock) {
        return new Account(id, ProductType.SAVINGS_ACCOUNT, new NoOverdraftPolicy(), clock);
    }

    public static Account openCurrentAccount(Money overdraftLimit, Clock clock) {
        return new Account(AccountId.newId(), ProductType.CURRENT_ACCOUNT,
                new ArrangedOverdraftPolicy(overdraftLimit), clock);
    }

    public AccountId id() {
        return id;
    }

    public Money balance() {
        return balance;
    }

    public ProductType productType() {
        return productType;
    }

    public void deposit(Money amount) {
        requireValidAmount(amount);
        balance = balance.add(amount);
        record(EntryType.DEPOSIT, amount);
    }

    public void withdraw(Money amount) {
        requireValidAmount(amount);
        if (!withdrawalPolicy.allows(balance, amount)) {
            throw new IllegalStateException("Withdrawal of " + amount + " not allowed, balance is " + balance);
        }
        balance = balance.subtract(amount);
        record(EntryType.WITHDRAWAL, amount);
    }

    public List<LedgerEntry> entries() {
        return List.copyOf(entries);
    }

    private void requireValidAmount(Money amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }
    }

    private void record(EntryType type, Money amount) {
        entries.add(new LedgerEntry(UUID.randomUUID(), id, type, amount, balance, clock.instant()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account other = (Account) o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
