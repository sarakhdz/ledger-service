package io.github.sarakhdz.ledger.domain.account;

import java.math.BigInteger;
import java.util.Objects;

public record Iban(String value) {

    public Iban {
        Objects.requireNonNull(value, "value must not be null");
        if (!value.matches("NL\\d{2}[A-Z]{4}\\d{10}")) {
            throw new InvalidIbanException("wrong format");
        }
        String rearranged = value.substring(4) + value.substring(0, 4);
        if (mod97(rearranged) != 1) {
            throw new InvalidIbanException("check digits do not match");
        }
    }

    public static Iban parse(String raw) {
        Objects.requireNonNull(raw, "raw must not be null");
        return new Iban(raw.replace(" ", "").toUpperCase());
    }

    static int mod97(String text) {
        StringBuilder digits = new StringBuilder();
        char[] chars = text.toCharArray();
        for (char c : chars) {
            digits.append(Character.getNumericValue(c));
        }

        return new BigInteger(digits.toString()).mod(BigInteger.valueOf(97)).intValue();
    }

}
