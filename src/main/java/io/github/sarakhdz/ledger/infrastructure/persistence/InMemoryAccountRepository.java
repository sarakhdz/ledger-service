package io.github.sarakhdz.ledger.infrastructure.persistence;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.account.AccountId;
import io.github.sarakhdz.ledger.domain.account.AccountRepository;
import io.github.sarakhdz.ledger.domain.iban.Iban;

import java.util.Optional;

public final class InMemoryAccountRepository extends InMemoryRepository<Account, AccountId>
        implements AccountRepository {

    @Override
    protected AccountId idOf(Account account) {
        return account.id();
    }

    @Override
    public Optional<Account> findByIban(Iban iban) {
        for (Account account : store.values()) {
            if (account.iban().equals(iban)) {
                return Optional.of(account);
            }
        }
        return Optional.empty();
    }
}
