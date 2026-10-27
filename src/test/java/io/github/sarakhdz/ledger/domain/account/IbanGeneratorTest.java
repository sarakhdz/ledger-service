package io.github.sarakhdz.ledger.domain.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IbanGeneratorTest {

    @Test
    void generatesValidDutchIbansForBankCodeLedg() {
        IbanGenerator generator = new IbanGenerator();

        assertThat(generator.forAccountNumber(1)).isEqualTo(Iban.parse("NL82LEDG0000000001"));
        assertThat(generator.forAccountNumber(123_456_789)).isEqualTo(Iban.parse("NL26LEDG0123456789"));
        assertThat(generator.forAccountNumber(9_876_543_210L)).isEqualTo(Iban.parse("NL60LEDG9876543210"));
    }
}
