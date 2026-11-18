package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.AccountId;
import io.github.sarakhdz.ledger.domain.account.AccountRepository;

import java.time.YearMonth;
import java.util.Objects;

public final class StatementService {

    private final AccountRepository repository;

    public StatementService(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public Statement generate(AccountId id, YearMonth month) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
