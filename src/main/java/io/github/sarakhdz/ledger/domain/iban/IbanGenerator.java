package io.github.sarakhdz.ledger.domain.iban;

public class IbanGenerator {

    public Iban forAccountNumber(long accountNumber) {
        String bban = "LEDG" + String.format("%010d", accountNumber);
        int check = 98 - Iban.mod97(bban + "NL00");
        return new Iban("NL" + String.format("%02d", check) + bban);
    }
}
