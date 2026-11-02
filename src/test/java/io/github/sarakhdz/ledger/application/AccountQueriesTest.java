package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.account.LedgerEntry;
import io.github.sarakhdz.ledger.domain.iban.IbanGenerator;
import io.github.sarakhdz.ledger.domain.money.Money;
import io.github.sarakhdz.ledger.infrastructure.persistence.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountQueriesTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneOffset.UTC);
    private final IbanGenerator ibans = new IbanGenerator();
    private final InMemoryAccountRepository repository = new InMemoryAccountRepository();
    private final AccountQueries queries = new AccountQueries(repository);

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


    private Account openCurrentAccount(long accountNumber) {
        Account account = Account.openCurrentAccount(
                ibans.forAccountNumber(accountNumber),
                Money.of("500.00", "EUR"),
                clock);
        repository.save(account);
        return account;
    }
}
