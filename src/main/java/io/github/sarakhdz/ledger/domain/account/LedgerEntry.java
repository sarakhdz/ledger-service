package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.account.AccountId;
import io.github.sarakhdz.ledger.domain.account.EntryType;
import io.github.sarakhdz.ledger.domain.money.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record LedgerEntry(UUID entryId, AccountId accountId, EntryType type,
                          Money amount, Money balanceAfter, Instant occurredAt) {

    public LedgerEntry {
        Objects.requireNonNull(entryId, "entryId must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(balanceAfter, "balanceAfter must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");

        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Entry amount must be positive, got: " + amount);
        }
    }
}
