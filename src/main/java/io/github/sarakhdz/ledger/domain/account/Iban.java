package io.github.sarakhdz.ledger.domain.account;

import java.util.Objects;

public record Iban(String value) {

    public Iban {
        Objects.requireNonNull(value, "value must not be null");
        if (!value.matches("NL\\d{2}[A-Z]{4}\\d{10}")) {
            throw new InvalidIbanException("wrong format");
        }
    }

    public static Iban parse(String raw) {
        Objects.requireNonNull(raw, "raw must not be null");
        return new Iban(raw.replace(" ", "").toUpperCase());
    }

}
