package pluralsight.m9.security;

public enum Roles {
    CUSTOMER,
    CUSTOMER_SERVICE,
    CUSTOMER_SERVICE_MANAGER;

    public String getGrantedAuthorityName() {
        return "ROLE_" + this.name();
    }
}
