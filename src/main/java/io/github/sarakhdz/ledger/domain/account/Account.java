package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;

import java.util.Objects;

public final class Account {

    private final AccountId id;
    private final ProductType productType;
    private final WithdrawalPolicy withdrawalPolicy;
    private Money balance;

    private Account(AccountId id, ProductType productType, WithdrawalPolicy withdrawalPolicy) {
        this.id = id;
        this.balance = Money.zero("EUR");
        this.productType = productType;
        this.withdrawalPolicy = withdrawalPolicy;
    }

    public static Account openSavingsAccount() {
        return new Account(AccountId.newId(), ProductType.SAVINGS_ACCOUNT,
                new NoOverdraftPolicy());
    }

    public static Account openCurrentAccount(Money overdraftLimit) {
        return new Account(AccountId.newId(), ProductType.CURRENT_ACCOUNT,
                new ArrangedOverdraftPolicy(overdraftLimit));
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
    }

    public void withdraw(Money amount) {
        requireValidAmount(amount);
        if (!withdrawalPolicy.allows(balance, amount)) {
            throw new IllegalStateException("Withdrawal of " + amount + " not allowed, balance is " + balance);
        }
        balance = balance.subtract(amount);
    }

    private void requireValidAmount(Money amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }
    }


}
