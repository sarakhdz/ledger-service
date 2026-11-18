package io.github.sarakhdz.ledger.application;

import io.github.sarakhdz.ledger.domain.account.Account;
import io.github.sarakhdz.ledger.domain.account.EntryType;
import io.github.sarakhdz.ledger.domain.account.LedgerEntry;
import io.github.sarakhdz.ledger.domain.iban.IbanGenerator;
import io.github.sarakhdz.ledger.domain.money.Money;
import io.github.sarakhdz.ledger.infrastructure.persistence.InMemoryAccountRepository;
import io.github.sarakhdz.ledger.testsupport.TestClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StatementServiceTest {

    private static final ZoneId AMSTERDAM = ZoneId.of("Europe/Amsterdam");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");
    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    private final IbanGenerator ibans = new IbanGenerator();
    private final InMemoryAccountRepository repository = new InMemoryAccountRepository();
    private final StatementService service = new StatementService(repository);
    private final TestClock clock = new TestClock(Instant.parse("2026-09-01T10:00:00Z"));

    @Test
    void showsTheAccountsIbanAndTheMonth() {
        Account account = openAccount();
        depositAt(account, "2026-10-05T10:00:00Z", "200.00");

        Statement statement = service.generate(account.id(), OCTOBER);

        assertThat(statement.iban()).isEqualTo(account.iban());
        assertThat(statement.month()).isEqualTo(OCTOBER);
    }

    @Test
    void listsTheMonthsEntriesOldestFirst() {
        Account account = openAccount();
        depositAt(account, "2026-10-05T10:00:00Z", "200.00");
        withdrawAt(account, "2026-10-12T10:00:00Z", "50.00");
        depositAt(account, "2026-10-20T10:00:00Z", "30.00");

        Statement statement = service.generate(account.id(), OCTOBER);

        assertThat(statement.entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("200.00"), eur("50.00"), eur("30.00"));
    }

    @Test
    void leavesOutEntriesFromOtherMonths() {
        Account account = openAccount();
        depositAt(account, "2026-09-15T10:00:00Z", "1000.00");
        depositAt(account, "2026-10-05T10:00:00Z", "200.00");
        withdrawAt(account, "2026-11-03T10:00:00Z", "15.00");

        Statement statement = service.generate(account.id(), OCTOBER);

        assertThat(statement.entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("200.00"));
    }

    @Test
    void totalsMoneyInAndMoneyOut() {
        Account account = openAccount();
        depositAt(account, "2026-09-15T10:00:00Z", "1000.00");   // September: must not count
        depositAt(account, "2026-10-05T10:00:00Z", "200.00");
        withdrawAt(account, "2026-10-12T10:00:00Z", "50.00");
        depositAt(account, "2026-10-20T10:00:00Z", "30.00");
        withdrawAt(account, "2026-10-28T10:00:00Z", "20.00");
        withdrawAt(account, "2026-11-03T10:00:00Z", "15.00");    // November: must not count

        Statement statement = service.generate(account.id(), OCTOBER);

        assertThat(statement.totalIn()).isEqualTo(eur("230.00"));
        assertThat(statement.totalOut()).isEqualTo(eur("70.00"));
    }

    @Test
    void countsEntriesPerType() {
        Account account = openAccount();
        depositAt(account, "2026-09-15T10:00:00Z", "1000.00");   // September: must not count
        depositAt(account, "2026-10-05T10:00:00Z", "200.00");
        withdrawAt(account, "2026-10-12T10:00:00Z", "50.00");
        withdrawAt(account, "2026-10-20T10:00:00Z", "30.00");
        withdrawAt(account, "2026-10-28T10:00:00Z", "20.00");

        Statement statement = service.generate(account.id(), OCTOBER);

        assertThat(statement.entriesPerType())
                .isEqualTo(Map.of(EntryType.DEPOSIT, 1L, EntryType.WITHDRAWAL, 3L));
    }

    @Test
    void entryAfterMidnightAmsterdamTimeBelongsToNovember() {
        Account account = openAccount();
        depositAt(account, "2026-10-31T23:30:00Z", "10.00");    // 00:30 on 1 November in Amsterdam

        assertThat(service.generate(account.id(), NOVEMBER).entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("10.00"));
        assertThat(service.generate(account.id(), OCTOBER).entries()).isEmpty();
    }

    @Test
    void sameMomentBelongsToDifferentMonthsInDifferentZones() {
        Account tokyo = openAccount(TOKYO, 1);
        Account amsterdam = openAccount(AMSTERDAM, 2);
        depositAt(tokyo, "2026-10-31T15:30:00Z", "10.00");      // 00:30 on 1 November in Tokyo
        depositAt(amsterdam, "2026-10-31T15:30:00Z", "20.00");  // 16:30 on 31 October in Amsterdam

        assertThat(service.generate(tokyo.id(), NOVEMBER).entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("10.00"));
        assertThat(service.generate(amsterdam.id(), OCTOBER).entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("20.00"));
    }

    @Test
    void monthBorderIsExactlyLocalMidnight() {
        Account account = openAccount();
        depositAt(account, "2026-10-31T22:59:59Z", "10.00");    // 23:59:59 on 31 October in Amsterdam
        depositAt(account, "2026-10-31T23:00:00Z", "20.00");    // 00:00:00 on 1 November in Amsterdam

        assertThat(service.generate(account.id(), OCTOBER).entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("10.00"));
        assertThat(service.generate(account.id(), NOVEMBER).entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("20.00"));
    }

    @Test
    void octoberStartsAtLocalMidnightInSummerTime() {
        Account account = openAccount();
        depositAt(account, "2026-09-30T21:59:59Z", "10.00");    // 23:59:59 on 30 September in Amsterdam (UTC+2)
        depositAt(account, "2026-09-30T22:00:00Z", "20.00");    // 00:00:00 on 1 October in Amsterdam

        assertThat(service.generate(account.id(), OCTOBER).entries())
                .extracting(LedgerEntry::amount)
                .containsExactly(eur("20.00"));
    }

    private Account openAccount() {
        return openAccount(AMSTERDAM, 1);
    }

    private Account openAccount(ZoneId zone, long accountNumber) {
        Account account = Account.openSavingsAccount(ibans.forAccountNumber(accountNumber), zone, clock);
        repository.save(account);
        return account;
    }

    private void depositAt(Account account, String instant, String amount) {
        clock.moveTo(Instant.parse(instant));
        account.deposit(eur(amount));
    }

    private void withdrawAt(Account account, String instant, String amount) {
        clock.moveTo(Instant.parse(instant));
        account.withdraw(eur(amount));
    }

    private static Money eur(String amount) {
        return Money.of(amount, "EUR");
    }
}
