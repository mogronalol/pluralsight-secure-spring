package pluralsight.m2.domain;

public enum AccountType {
    CHECKING("Checking Account"), CREDIT_CARD("Credit Card");

    private final String displayName;

    AccountType(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
