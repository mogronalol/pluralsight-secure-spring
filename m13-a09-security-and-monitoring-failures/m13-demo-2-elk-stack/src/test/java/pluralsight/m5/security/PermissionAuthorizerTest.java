package pluralsight.m5.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pluralsight.m13.config.SecurityConfig;
import pluralsight.m13.model.TransferModel;
import pluralsight.m13.security.PermissionAuthorizer;
import pluralsight.m13.security.Roles;
import pluralsight.m5.util.AllowedRoles;
import pluralsight.m5.util.AllowedRolesArgumentProvider;
import pluralsight.m5.util.DoubleArgumentsPerRole;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static pluralsight.m5.util.TestAccountBuilder.testAccountBuilder;

class PermissionAuthorizerTest {
    private PermissionAuthorizer permissionAuthorizer;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        permissionAuthorizer = new PermissionAuthorizer(new SecurityConfig().roleHierarchy());
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER,
            Roles.SENIOR_VICE_PRESIDENT, Roles.CUSTOMER, Roles.HUMAN_RESOURCES})
    public void canViewAnyAccountThatBelongsToUser(
            Authentication authentication,
            boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(permissionAuthorizer.getCanViewAccount(testAccountBuilder().build()))
                .isEqualTo(permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER,
            Roles.SENIOR_VICE_PRESIDENT})
    public void canViewAnyAccountThatBelongsToAnyUserWithViewAccountsAuthority(
            Authentication authentication,
            boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(
                permissionAuthorizer.getCanViewAccount(testAccountBuilder().username("other").build()))
                .isEqualTo(permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER,
            Roles.SENIOR_VICE_PRESIDENT})
    @DoubleArgumentsPerRole({0, 1, 499.99})
    public void onlyAuthorizedUsersShouldPerformTransfers(Authentication authentication,
                                                          double transferAmount,
                                                          boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(permissionAuthorizer.canPerformTransfer(TransferModel.builder()
                        .fromAccountCode("from")
                        .toAccountCode("to")
                        .amount(BigDecimal.valueOf(transferAmount))
                        .build())).isEqualTo(permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER, Roles.SENIOR_VICE_PRESIDENT})
    @DoubleArgumentsPerRole({500, 500.01, Double.MAX_VALUE})
    public void onlyAuthorizedUsersShouldPerformLargeTransfers(Authentication authentication,
                                                               double transferAmount,
                                                               boolean permitted) {

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(permissionAuthorizer.canPerformTransfer(TransferModel.builder()
                .fromAccountCode("from")
                .toAccountCode("to")
                .amount(BigDecimal.valueOf(transferAmount))
                .build())).isEqualTo(permitted);
    }

}