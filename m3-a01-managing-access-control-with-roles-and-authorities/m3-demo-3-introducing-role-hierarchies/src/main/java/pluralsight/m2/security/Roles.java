package pluralsight.m2.security;

public enum Roles {
    CUSTOMER_SERVICE,
    CUSTOMER_SERVICE_MANAGER,
    HUMAN_RESOURCES,
    SENIOR_VICE_PRESIDENT;

    public String getGrantedAuthorityName() {
        return "ROLE_" + this.name();
    }
}
