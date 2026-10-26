package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LedgerEntryTest {

    @Test
    void rejectsNonPositiveAmounts() {
        AccountId accountId = AccountId.newId();
        Instant now = Instant.parse("2026-10-26T10:00:00Z");

        assertThatThrownBy(() -> new LedgerEntry(UUID.randomUUID(), accountId, EntryType.DEPOSIT,
                Money.zero("EUR"), Money.zero("EUR"), now))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
