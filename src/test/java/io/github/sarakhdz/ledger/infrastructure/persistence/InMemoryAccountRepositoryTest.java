package io.github.sarakhdz.ledger.infrastructure.persistence;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.account.AccountId;
import io.github.sarakhdz.ledger.domain.iban.IbanGenerator;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryAccountRepositoryTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-27T10:00:00Z"), ZoneOffset.UTC);
    private final IbanGenerator ibans = new IbanGenerator();
    private final InMemoryAccountRepository repository = new InMemoryAccountRepository();

    @Test
    void savedAccountCanBeFoundByIdAndIban() {
        Account account = Account.openSavingsAccount(ibans.forAccountNumber(1), clock);
        repository.save(account);

        assertThat(repository.findById(account.id())).contains(account);
        assertThat(repository.findByIban(account.iban())).contains(account);
    }

    @Test
    void unknownIdOrIbanGivesEmpty() {
        assertThat(repository.findById(AccountId.newId())).isEmpty();
        assertThat(repository.findByIban(ibans.forAccountNumber(99))).isEmpty();
    }

    @Test
    void savingTheSameAccountTwiceKeepsOne() {
        Account account = Account.openSavingsAccount(ibans.forAccountNumber(1), clock);
        repository.save(account);
        repository.save(account);

        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void findAllReturnsEveryAccountAsAReadOnlyList() {
        repository.save(Account.openSavingsAccount(ibans.forAccountNumber(1), clock));
        repository.save(Account.openSavingsAccount(ibans.forAccountNumber(2), clock));

        List<Account> all = repository.findAll();
        assertThat(all).hasSize(2);
        assertThatThrownBy(() -> all.clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
