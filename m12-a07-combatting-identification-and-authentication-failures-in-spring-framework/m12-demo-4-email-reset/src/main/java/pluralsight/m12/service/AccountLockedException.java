package pluralsight.m12.service;

import lombok.EqualsAndHashCode;

import java.time.Duration;

@EqualsAndHashCode(callSuper = true)
public class AccountLockedException extends RuntimeException{
    private final Duration timeRemaining;
    private final String username;

    public AccountLockedException(final Duration timeRemaining, final String username) {
        super("Account %s is locked for %s".formatted(username, timeRemaining));
        this.timeRemaining = timeRemaining;
        this.username = username;
    }
}
