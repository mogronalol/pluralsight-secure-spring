package pluralsight.m13.service;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(final String username) {
        super("Account locked for user " + username);
    }
}
