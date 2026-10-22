package io.github.sarakhdz.ledger.domain.account;

import io.github.sarakhdz.ledger.domain.money.Money;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class AccountTest {

    @Test
    void newSavingsAccountStartsAtZero() {
        Account account = Account.openSavingsAccount();

        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));
        assertThat(account.productType()).isEqualTo(ProductType.SAVINGS_ACCOUNT);
    }

    @Test
    void depositIncreasesTheBalance() {
        Account account = Account.openSavingsAccount();
        account.deposit(Money.of("100.00", "EUR"));
        account.deposit(Money.of("25.50", "EUR"));

        assertThat(account.balance()).isEqualTo(Money.of("125.50", "EUR"));
    }

    @Test
    void rejectsZeroNegativeAndForeignDeposits() {
        Account account = Account.openSavingsAccount();

        assertThatThrownBy(() -> account.deposit(Money.of("0.00", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.deposit(Money.of("-5.00", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.deposit(Money.of("5.00", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));   // nothing changed
    }

    @Test
    void savingsCanWithdrawTheFullBalanceButNotOneCentMore() {
        Account account = Account.openSavingsAccount();
        account.deposit(Money.of("100.00", "EUR"));

        assertThatThrownBy(() -> account.withdraw(Money.of("100.01", "EUR")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(account.balance()).isEqualTo(Money.of("100.00", "EUR"));   // unchanged

        account.withdraw(Money.of("100.00", "EUR"));
        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));
    }

    @Test
    void currentAccountCanGoDownToTheOverdraftLimitButNotBeyond() {
        Account account = Account.openCurrentAccount(Money.of("500.00", "EUR"));

        assertThat(account.productType()).isEqualTo(ProductType.CURRENT_ACCOUNT);

        account.withdraw(Money.of("500.00", "EUR"));
        assertThat(account.balance()).isEqualTo(Money.of("-500.00", "EUR"));

        assertThatThrownBy(() -> account.withdraw(Money.of("0.01", "EUR")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(account.balance()).isEqualTo(Money.of("-500.00", "EUR"));
    }

    @Test
    void rejectsNegativeOverdraftLimit() {
        assertThatThrownBy(() -> Account.openCurrentAccount(Money.of("-1.00", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroNegativeAndForeignWithdrawals() {
        Account account = Account.openCurrentAccount(Money.of("100.00", "EUR"));

        assertThatThrownBy(() -> account.withdraw(Money.of("0.00", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.withdraw(Money.of("-5.00", "EUR")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.withdraw(Money.of("5.00", "USD")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(account.balance()).isEqualTo(Money.zero("EUR"));
    }

    @Test
    void accountsWithTheSameIdAreEqualWhateverTheirBalance() {
        AccountId id = AccountId.newId();
        Account first = Account.openSavingsAccount(id);
        Account second = Account.openSavingsAccount(id);
        second.deposit(Money.of("50.00", "EUR"));

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(Account.openSavingsAccount());   // different id
    }
}
