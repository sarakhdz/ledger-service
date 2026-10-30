package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.account.AccountRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class AccountQueries {

    private final AccountRepository repository;

    public AccountQueries(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public List<Account> accountsInOverdraft() {
        return repository.findAll().stream()
                .filter(account -> account.balance().isNegative())
                .sorted(Comparator.comparing(account -> account.balance().amount()))
                .toList();
    }

}
