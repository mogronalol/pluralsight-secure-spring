package pluralsight.m12.domain;

import lombok.Getter;

@Getter public enum AccountType {
    CHECKING("Checking Account"), CREDIT_CARD("Credit Card");

    private final String displayName;

    AccountType(final String displayName) {
        this.displayName = displayName;
    }

}
