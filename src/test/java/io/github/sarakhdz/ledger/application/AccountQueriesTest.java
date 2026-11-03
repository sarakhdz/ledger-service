package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.account.AccountId;
import io.github.sarakhdz.ledger.domain.account.AccountNotFoundException;
import io.github.sarakhdz.ledger.domain.account.LedgerEntry;
import io.github.sarakhdz.ledger.domain.iban.IbanGenerator;
import io.github.sarakhdz.ledger.domain.money.Money;
import io.github.sarakhdz.ledger.infrastructure.persistence.InMemoryAccountRepository;
import io.github.sarakhdz.ledger.testsupport.TestClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountQueriesTest {

    private final IbanGenerator ibans = new IbanGenerator();
    private final InMemoryAccountRepository repository = new InMemoryAccountRepository();
    private final AccountQueries queries = new AccountQueries(repository);
    private final TestClock clock = new TestClock(Instant.parse("2026-10-01T09:00:00Z"));

    @Test
    void returnsOnlyAccountsWithNegativeBalance() {
        Account positive = openCurrentAccount(1);
        positive.deposit(Money.of("50.00", "EUR"));
        Account zero = openCurrentAccount(2);
        Account negative = openCurrentAccount(3);
        negative.withdraw(Money.of("20.00", "EUR"));

        List<Account> result = queries.accountsInOverdraft();

        assertThat(result).containsExactly(negative);
    }

    @Test
    void returnsEmptyListWhenNoAccountIsOverdraft() {
        Account positive = openCurrentAccount(1);
        positive.deposit(Money.of("50.00", "EUR"));
        Account zero = openCurrentAccount(2);

        List<Account> result = queries.accountsInOverdraft();

        assertThat(result).isEmpty();
    }

    @Test
    void sortsMostNegativeFirst() {
        Account minus20 = openCurrentAccount(1);
        minus20.withdraw(Money.of("20.00", "EUR"));
        Account minus300 = openCurrentAccount(2);
        minus300.withdraw(Money.of("300.00", "EUR"));
        Account minus100 = openCurrentAccount(3);
        minus100.withdraw(Money.of("100.00", "EUR"));

        List<Account> result = queries.accountsInOverdraft();

        assertThat(result).containsExactly(minus300, minus100, minus20);
    }

    @Test
    void returnsTheNBiggestWithdrawalsAcrossAllAccountsBiggestFirst() {
        Account first = openCurrentAccount(1);
        first.withdraw(Money.of("10.00", "EUR"));
        first.withdraw(Money.of("50.00", "EUR"));
        Account second = openCurrentAccount(2);
        second.withdraw(Money.of("30.00", "EUR"));
        second.withdraw(Money.of("70.00", "EUR"));

        List<LedgerEntry> result = queries.largestWithdrawals(3);

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("70.00", "EUR"),
                        Money.of("50.00", "EUR"),
                        Money.of("30.00", "EUR"));
    }

    @Test
    void ignoresDeposits() {
        Account first = openCurrentAccount(1);
        first.deposit(Money.of("1000.00", "EUR"));
        first.withdraw(Money.of("20.00", "EUR"));

        List<LedgerEntry> result = queries.largestWithdrawals(2);

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("20.00", "EUR"));
    }

    @Test
    void returnsAllWithdrawalsWhenThereAreFewerThanN() {
        Account first = openCurrentAccount(1);
        first.withdraw(Money.of("10.00", "EUR"));
        first.withdraw(Money.of("20.00", "EUR"));

        List<LedgerEntry> result = queries.largestWithdrawals(5);

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("20.00", "EUR"), Money.of("10.00", "EUR"));
    }

    @Test
    void returnsEmptyListWhenNIsZero() {
        Account first = openCurrentAccount(1);
        first.withdraw(Money.of("10.00", "EUR"));

        List<LedgerEntry> result = queries.largestWithdrawals(0);

        assertThat(result).isEmpty();
    }

    @Test
    void rejectsNegativeN() {
        assertThatThrownBy(() -> queries.largestWithdrawals(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returnsEmptyListWhenThereAreNoWithdrawals() {
        openCurrentAccount(1);

        List<LedgerEntry> result = queries.largestWithdrawals(5);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsOnlyEntriesInsideThePeriod() {
        Account account = openCurrentAccount(1);
        account.deposit(Money.of("10.00", "EUR"));
        clock.advance(Duration.ofHours(1));
        account.deposit(Money.of("20.00", "EUR"));
        clock.advance(Duration.ofHours(1));
        account.deposit(Money.of("30.00", "EUR"));
        clock.advance(Duration.ofHours(1));
        account.deposit(Money.of("40.00", "EUR"));

        List<LedgerEntry> result = queries.entriesBetween(account.id(),
                Instant.parse("2026-10-01T09:30:00Z"),
                Instant.parse("2026-10-01T11:30:00Z"));

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("20.00", "EUR"), Money.of("30.00", "EUR"));
    }

    @Test
    void ignoresEntriesOfOtherAccounts() {
        Account mine = openCurrentAccount(1);
        Account other = openCurrentAccount(2);
        mine.deposit(Money.of("10.00", "EUR"));
        other.deposit(Money.of("20.00", "EUR"));

        List<LedgerEntry> result = queries.entriesBetween(mine.id(),
                Instant.parse("2026-10-01T09:00:00Z"),
                Instant.parse("2026-10-01T10:00:00Z"));

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("10.00", "EUR"));
    }

    @Test
    void includesAnEntryExactlyAtFrom() {
        Account account = openCurrentAccount(1);
        account.deposit(Money.of("10.00", "EUR"));

        List<LedgerEntry> result = queries.entriesBetween(account.id(),
                Instant.parse("2026-10-01T09:00:00Z"),
                Instant.parse("2026-10-01T10:00:00Z"));

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("10.00", "EUR"));
    }

    @Test
    void excludesAnEntryExactlyAtTo() {
        Account account = openCurrentAccount(1);
        account.deposit(Money.of("10.00", "EUR"));
        clock.advance(Duration.ofHours(1));
        account.deposit(Money.of("20.00", "EUR"));

        List<LedgerEntry> result = queries.entriesBetween(account.id(),
                Instant.parse("2026-10-01T09:00:00Z"),
                Instant.parse("2026-10-01T10:00:00Z"));

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("10.00", "EUR"));

    }

    @Test
    void throwsAccountNotFoundForAnUnknownAccount() {
        assertThatThrownBy(() -> queries.entriesBetween(AccountId.newId(),
                Instant.parse("2026-10-01T09:00:00Z"),
                Instant.parse("2026-10-01T10:00:00Z")))
                .isInstanceOfSatisfying(AccountNotFoundException.class,
                        e -> assertThat(e.errorCode()).isEqualTo("ACCOUNT_NOT_FOUND"));
    }

    @Test
    void rejectsFromAfterTo() {
        Account account = openCurrentAccount(1);

        assertThatThrownBy(() -> queries.entriesBetween(account.id(),
                Instant.parse("2026-10-01T10:00:00Z"),
                Instant.parse("2026-10-01T09:00:00Z")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void returnsEmptyListWhenNothingHappenedInThePeriod() {
        Account account = openCurrentAccount(1);
        account.deposit(Money.of("10.00", "EUR"));

        List<LedgerEntry> result = queries.entriesBetween(account.id(),
                Instant.parse("2026-10-01T10:00:00Z"),
                Instant.parse("2026-10-01T10:30:00Z"));

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEntriesOldestFirst() {
        Account account = openCurrentAccount(1);
        account.deposit(Money.of("20.00", "EUR"));
        clock.advance(Duration.ofHours(1));
        account.deposit(Money.of("10.00", "EUR"));
        clock.advance(Duration.ofHours(1));
        account.deposit(Money.of("30.00", "EUR"));

        List<LedgerEntry> result = queries.entriesBetween(account.id(),
                Instant.parse("2026-10-01T09:00:00Z"),
                Instant.parse("2026-10-01T11:30:00Z"));

        assertThat(result)
                .extracting(LedgerEntry::amount)
                .containsExactly(Money.of("20.00", "EUR"),
                        Money.of("10.00", "EUR"),
                        Money.of("30.00", "EUR"));
    }


    private Account openCurrentAccount(long accountNumber) {
        Account account = Account.openCurrentAccount(
                ibans.forAccountNumber(accountNumber),
                Money.of("500.00", "EUR"),
                clock);
        repository.save(account);
        return account;
    }
}
