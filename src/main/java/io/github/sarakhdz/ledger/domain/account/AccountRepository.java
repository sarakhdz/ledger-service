package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.shared.Repository;

import java.util.Optional;

public interface AccountRepository extends Repository<Account, AccountId> {

    Optional<Account> findByIban(String iban);
}
