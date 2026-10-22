package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;

import java.util.Objects;

public class Account {

    private final AccountId id;
    private Money balance;
    private final ProductType productType;

    private Account(AccountId id, ProductType productType) {
        this.id = id;
        this.balance = Money.zero("EUR");
        this.productType = productType;
    }

    public static Account openSavingsAccount() {
        return new Account(AccountId.newId(), ProductType.SAVINGS_ACCOUNT);
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

    private void requireValidAmount(Money amount){
        Objects.requireNonNull(amount, "amount must not be null");
        if(!amount.isPositive()){
            throw new IllegalArgumentException("cannot deposit negative amount: " + amount + "");
        }
    }


}
