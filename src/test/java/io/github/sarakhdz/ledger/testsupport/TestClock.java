package io.github.sarakhdz.ledger.testsupport;

import java.time.*;

public final class TestClock extends Clock {

    private Instant now;

    public TestClock(Instant start) {
        this.now = start;
    }

    public void advance(Duration duration) {
        now = now.plus(duration);
    }

    public void moveTo(Instant target) {
        if (target.isBefore(now)) {
            throw new IllegalArgumentException("TestClock cannot go back in time: " + now + " -> " + target);
        }
        now = target;
    }

    @Override
    public Instant instant() {
        return now;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException();
    }

}
