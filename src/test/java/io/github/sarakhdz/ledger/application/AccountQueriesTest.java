package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.iban.IbanGenerator;
import io.github.sarakhdz.ledger.domain.money.Money;
import io.github.sarakhdz.ledger.infrastructure.persistence.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Account openCurrentAccount(long accountNumber) {
        Account account = Account.openCurrentAccount(
                ibans.forAccountNumber(accountNumber),
                Money.of("500.00", "EUR"),
                clock);
        repository.save(account);
        return account;
    }

}
