package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.EntryType;
import io.github.sarakhdz.ledger.domain.account.LedgerEntry;
import io.github.sarakhdz.ledger.domain.iban.Iban;
import io.github.sarakhdz.ledger.domain.money.Money;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public record Statement(Iban iban,
                        YearMonth month,
                        Money openingBalance,
                        Money closingBalance,
                        List<LedgerEntry> entries,
                        Money totalIn,
                        Money totalOut,
                        Map<EntryType, Long> entriesPerType) {

    public Statement {
        entries = List.copyOf(entries);
        entriesPerType = Map.copyOf(entriesPerType);
    }
}
