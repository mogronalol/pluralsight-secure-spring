package pluralsight.m16.domain;

import lombok.Getter;

@Getter
public enum AccountType {
    CHECKING("Checking Account"), CREDIT_CARD("Credit Card"), CURRENT("Current account");

    private final String displayName;

    AccountType(final String displayName) {
        this.displayName = displayName;
    }

}
