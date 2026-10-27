package io.github.sarakhdz.ledger.domain.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IbanTest {

    @Test
    void parseRemovesSpacesAndUppercases() {
        Iban iban = Iban.parse("nl91 abna 0417 1643 00");
        assertThat(iban.value()).isEqualTo("NL91ABNA0417164300");
    }

    @Test
    void rejectsWrongFormat() {
        assertThatThrownBy(() -> Iban.parse("NL91ABNA041716430"))      // 17 characters
                .isInstanceOf(InvalidIbanException.class);
        assertThatThrownBy(() -> Iban.parse("DE91ABNA0417164300"))     // not Dutch
                .isInstanceOf(InvalidIbanException.class);
        assertThatThrownBy(() -> Iban.parse("NL911BNA0417164300"))     // a digit where a letter belongs
                .isInstanceOf(InvalidIbanException.class);
        assertThatThrownBy(() -> Iban.parse("NL91AB_A0417164300"))     // a symbol in the bank code
                .isInstanceOf(InvalidIbanException.class);
    }

    @Test
    void acceptsValidCheckDigits() {
        assertThat(Iban.parse("NL91ABNA0417164300").value()).isEqualTo("NL91ABNA0417164300");
        assertThat(Iban.parse("NL26LEDG0123456789").value()).isEqualTo("NL26LEDG0123456789");
    }

    @Test
    void rejectsWrongCheckDigits() {
        assertThatThrownBy(() -> Iban.parse("NL91ABNA0417164301"))   // last digit changed
                .isInstanceOf(InvalidIbanException.class);
    }

    @Test
    void formatsAndMasks() {
        Iban iban = Iban.parse("NL91ABNA0417164300");

        assertThat(iban.formatted()).isEqualTo("NL91 ABNA 0417 1643 00");
        assertThat(iban.masked()).isEqualTo("NL91 **** **** **43 00");
        assertThat(iban).hasToString("NL91 **** **** **43 00");
    }

    @Test
    void errorMessagesNeverContainTheIban() {
        assertThatThrownBy(() -> Iban.parse("NL91ABNA0417164301"))
                .hasMessageNotContaining("0417164301");
    }

}
