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
}
