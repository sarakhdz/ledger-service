package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.account.AccountRepository;
import io.github.sarakhdz.ledger.domain.account.EntryType;
import io.github.sarakhdz.ledger.domain.account.LedgerEntry;

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

    public List<LedgerEntry> largestWithdrawals(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative, got: " + n);
        }
        return repository.findAll().stream()
                .flatMap(account -> account.entries().stream())
                .filter(entry -> entry.type() == EntryType.WITHDRAWAL)
                .sorted(Comparator.comparing(entry -> entry.amount().amount(), Comparator.reverseOrder()))
                .limit(n)
                .toList();
    }
}
