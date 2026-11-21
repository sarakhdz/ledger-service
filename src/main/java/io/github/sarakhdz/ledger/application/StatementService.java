package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.*;
import io.github.sarakhdz.ledger.domain.money.Money;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class StatementService {

    private final AccountRepository repository;

    public StatementService(AccountRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    public Statement generate(AccountId id, YearMonth month) {
        Account account = repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
        List<LedgerEntry> entries = account.entries();

        Instant start = month.atDay(1).atStartOfDay(account.zone()).toInstant();
        Instant end = month.plusMonths(1).atDay(1).atStartOfDay(account.zone()).toInstant();

        List<LedgerEntry> monthEntries = entries.stream()
                .filter(entry -> !entry.occurredAt().isBefore(start))
                .filter(entry -> entry.occurredAt().isBefore(end))
                .sorted(Comparator.comparing(LedgerEntry::occurredAt))
                .toList();

        Money opening = entries.stream()
                .filter(entry -> entry.occurredAt().isBefore(start))
                .reduce((earlier, later) -> later)
                .map(LedgerEntry::balanceAfter)
                .orElse(Money.zero("EUR"));

        Money closing = monthEntries.stream()
                .reduce((earlier, later) -> later)
                .map(LedgerEntry::balanceAfter)
                .orElse(opening);

        Money totalIn = monthEntries.stream()
                .filter(entry -> entry.type() == EntryType.DEPOSIT)
                .map(LedgerEntry::amount)
                .reduce(Money.zero("EUR"), Money::add);

        Money totalOut = monthEntries.stream()
                .filter(entry -> entry.type() == EntryType.WITHDRAWAL)
                .map(LedgerEntry::amount)
                .reduce(Money.zero("EUR"), Money::add);

        Map<EntryType, Long> counts = monthEntries.stream()
                .collect(Collectors.groupingBy(LedgerEntry::type, Collectors.counting()));

        return new Statement(account.iban(), month, opening, closing,
                monthEntries, totalIn, totalOut, counts);
    }
}
