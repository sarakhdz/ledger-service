package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.CurrencyMismatchException;
import io.github.sarakhdz.ledger.domain.money.InvalidAmountException;
import io.github.sarakhdz.ledger.domain.money.Money;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final Instant NOW = Instant.parse("2026-10-24T10:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void newSavingsAccountStartsAtZero() {
        Account account = Account.openSavingsAccount(clock);

        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));
        assertThat(account.productType()).isEqualTo(ProductType.SAVINGS_ACCOUNT);
    }

    @Test
    void depositIncreasesTheBalance() {
        Account account = Account.openSavingsAccount(clock);
        account.deposit(Money.of("100.00", "EUR"));
        account.deposit(Money.of("25.50", "EUR"));

        assertThat(account.balance()).isEqualTo(Money.of("125.50", "EUR"));
    }

    @Test
    void rejectsZeroNegativeAndForeignDeposits() {
        Account account = Account.openSavingsAccount(clock);

        assertThatThrownBy(() -> account.deposit(Money.of("0.00", "EUR")))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.deposit(Money.of("-5.00", "EUR")))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.deposit(Money.of("5.00", "USD")))
                .isInstanceOf(CurrencyMismatchException.class);
        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));   // nothing changed
    }

    @Test
    void savingsCanWithdrawTheFullBalanceButNotOneCentMore() {
        Account account = Account.openSavingsAccount(clock);
        account.deposit(Money.of("100.00", "EUR"));

        assertThatThrownBy(() -> account.withdraw(Money.of("100.01", "EUR")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.balance()).isEqualTo(Money.of("100.00", "EUR"));   // unchanged

        account.withdraw(Money.of("100.00", "EUR"));
        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));
    }

    @Test
    void currentAccountCanGoDownToTheOverdraftLimitButNotBeyond() {
        Account account = Account.openCurrentAccount(Money.of("500.00", "EUR"), clock);

        assertThat(account.productType()).isEqualTo(ProductType.CURRENT_ACCOUNT);

        account.withdraw(Money.of("500.00", "EUR"));
        assertThat(account.balance()).isEqualTo(Money.of("-500.00", "EUR"));

        assertThatThrownBy(() -> account.withdraw(Money.of("0.01", "EUR")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.balance()).isEqualTo(Money.of("-500.00", "EUR"));
    }

    @Test
    void rejectsNegativeOverdraftLimit() {
        assertThatThrownBy(() -> Account.openCurrentAccount(Money.of("-1.00", "EUR"), clock))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void rejectsZeroNegativeAndForeignWithdrawals() {
        Account account = Account.openCurrentAccount(Money.of("100.00", "EUR"), clock);

        assertThatThrownBy(() -> account.withdraw(Money.of("0.00", "EUR")))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.withdraw(Money.of("-5.00", "EUR")))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> account.withdraw(Money.of("5.00", "USD")))
                .isInstanceOf(CurrencyMismatchException.class);
        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));
    }

    @Test
    void accountsWithTheSameIdAreEqualWhateverTheirBalance() {
        AccountId id = AccountId.newId();
        Account first = Account.openSavingsAccount(id, clock);
        Account second = Account.openSavingsAccount(id, clock);
        second.deposit(Money.of("50.00", "EUR"));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(Account.openSavingsAccount(clock));   // different id
    }

    @Test
    void currentAccountWithZeroLimitCannotGoBelowZero() {
        Account account = Account.openCurrentAccount(Money.zero("EUR"), clock);
        account.deposit(Money.of("10.00", "EUR"));

        account.withdraw(Money.of("10.00", "EUR"));
        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));

        assertThatThrownBy(() -> account.withdraw(Money.of("0.01", "EUR")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void depositRecordsALedgerEntry() {
        Account account = Account.openSavingsAccount(clock);
        account.deposit(Money.of("100.00", "EUR"));

        assertThat(account.entries()).hasSize(1);
        LedgerEntry entry = account.entries().get(0);
        assertThat(entry.type()).isEqualTo(EntryType.DEPOSIT);
        assertThat(entry.amount()).isEqualTo(Money.of("100.00", "EUR"));
        assertThat(entry.balanceAfter()).isEqualTo(Money.of("100.00", "EUR"));
        assertThat(entry.accountId()).isEqualTo(account.id());
        assertThat(entry.occurredAt()).isEqualTo(NOW);
    }

    @Test
    void withdrawalRecordsAnEntryAndRejectedOperationsRecordNothing() {
        Account account = Account.openSavingsAccount(clock);
        account.deposit(Money.of("100.00", "EUR"));
        account.withdraw(Money.of("30.00", "EUR"));

        assertThatThrownBy(() -> account.withdraw(Money.of("500.00", "EUR")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThatThrownBy(() -> account.deposit(Money.of("0.00", "EUR")))
                .isInstanceOf(InvalidAmountException.class);

        assertThat(account.entries()).hasSize(2);      // the two failures added nothing
        LedgerEntry withdrawal = account.entries().get(1);
        assertThat(withdrawal.type()).isEqualTo(EntryType.WITHDRAWAL);
        assertThat(withdrawal.amount()).isEqualTo(Money.of("30.00", "EUR"));
        assertThat(withdrawal.balanceAfter()).isEqualTo(Money.of("70.00", "EUR"));
    }

    @Test
    void entriesCannotBeModifiedFromOutside() {
        Account account = Account.openSavingsAccount(clock);
        account.deposit(Money.of("10.00", "EUR"));

        List<LedgerEntry> entries = account.entries();
        assertThatThrownBy(() -> entries.clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(account.entries()).hasSize(1);
    }

    @Test
    void balanceAlwaysMatchesTheEntries() {
        Account account = Account.openCurrentAccount(Money.of("200.00", "EUR"), clock);
        account.deposit(Money.of("100.00", "EUR"));
        account.withdraw(Money.of("250.00", "EUR"));
        account.deposit(Money.of("40.00", "EUR"));

        Money total = Money.zero("EUR");
        for (LedgerEntry entry : account.entries()) {
            if (entry.type() == EntryType.DEPOSIT) {
                total = total.add(entry.amount());
            } else {
                total = total.subtract(entry.amount());
            }
        }
        assertThat(account.balance()).isEqualTo(total);
    }

    @Test
    void refusedWithdrawalCarriesItsErrorCode() {
        Account account = Account.openSavingsAccount(clock);

        assertThatThrownBy(() -> account.withdraw(Money.of("1.00", "EUR")))
                .isInstanceOfSatisfying(InsufficientFundsException.class,
                        e -> assertThat(e.errorCode()).isEqualTo("INSUFFICIENT_FUNDS"));
    }
}
