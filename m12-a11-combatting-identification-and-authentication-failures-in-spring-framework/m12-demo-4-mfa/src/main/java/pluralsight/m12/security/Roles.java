package pluralsight.m12.security;

public enum Roles {
    CUSTOMER_SERVICE,
    CUSTOMER_SERVICE_MANAGER,
    HUMAN_RESOURCES,
    SENIOR_VICE_PRESIDENT,
    CUSTOMER,
    PARTIAL_LOGIN_PENDING_OTP;

    public String getGrantedAuthorityName() {
        return "ROLE_" + this.name();
    }
}
