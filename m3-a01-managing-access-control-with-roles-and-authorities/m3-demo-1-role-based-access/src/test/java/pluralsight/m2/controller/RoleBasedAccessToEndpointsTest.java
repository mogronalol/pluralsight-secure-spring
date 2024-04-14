package pluralsight.m2.controller;

import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pluralsight.m2.repository.AccountRepository;
import pluralsight.m2.security.Roles;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pluralsight.m2.repository.TestDataFactory.generateAccount;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedAccessToEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER})
    public void verifyRoleBasedAccessToMyAccountsPage(Roles roles, boolean permitted)
            throws Exception {
        final ResultActions perform = mockMvc.perform(get("/my-accounts")
                .with(user("user").roles(roles.name()))
        );

        if (permitted) {
            perform
                    .andExpect(status().isOk())
                    .andExpect(content().string(
                            containsString("data-test-id=\"nav-my-accounts\"")
                    ))
                    .andExpect(content().string(
                            not(containsString("data-test-id=\"nav-admin-accounts\""))
                    ))
                    .andExpect(content().string(
                            not(containsString("data-test-id=\"nav-admin-transactions\""))
                    ));
        } else {
            perform.andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER})
    public void verifyRoleBasedAccessToAdminAccountsPage(Roles roles, boolean permitted)
            throws Exception {
        final ResultActions perform = mockMvc.perform(get("/admin/accounts")
                .with(user("user").roles(roles.name()))
        );

        if (permitted) {
            perform
                    .andExpect(status().isOk())
                    .andExpect(content().string(
                            not(containsString("data-test-id=\"nav-my-accounts\""))
                    ))
                    .andExpect(content().string(
                            containsString("data-test-id=\"nav-admin-accounts\"")
                    ));
        } else {
            perform.andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles(Roles.CUSTOMER_SERVICE_MANAGER)
    public void verifyRoleBasedAccessToAdminTransactionsLinkOnAdminAccountsPage(Roles roles,
                                                                                boolean permitted)
            throws Exception {

        final ResultActions perform = mockMvc.perform(get("/admin/accounts")
                .with(user("user").roles(roles.name())));

        final Matcher<String> adminTransactionsLinkMatcher =
                containsString("data-test-id=\"nav-admin-transactions\"");

        if (permitted) {
            perform
                    .andExpect(content().string(
                            adminTransactionsLinkMatcher
                    ));
        } else {
            perform
                    .andExpect(content().string(
                            not(adminTransactionsLinkMatcher)
                    ));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER})
    public void myAccountTransactionsIsSecured(final Roles role, final boolean permitted)
            throws Exception {

        accountRepository.save(generateAccount("user", 0));

        final ResultActions perform = mockMvc.perform(
                get("/accounts/0/transactions")
                        .with(user("user").roles(role.name())));

        if (permitted) {
            perform
                    .andExpect(status().is(200))
                    .andExpect(content().string(
                            containsString("data-test-id=\"nav-my-accounts\"")))
                    .andExpect(content().string(
                            not(containsString("data-test-id=\"nav-admin-accounts\""))))
                    .andExpect(content().string(
                            not(containsString("data-test-id=\"nav-admin-transactions\""))));
        } else {
            perform
                    .andExpect(status().isForbidden());
        }

        accountRepository.deleteAll();
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER})
    public void transferPageIsSecuredByRoles(final Roles role, final boolean permitted)
            throws Exception {

        final ResultActions perform = mockMvc.perform(
                get("/admin/transfer")
                        .with(user("user").roles(role.name())));

        if (permitted) {

            final Matcher<String> containsAdminTransactions =
                    containsString("data-test-id=\"nav-admin-transactions\"");

            perform
                    .andExpect(status().is(200))
                    .andExpect(content().string(
                            not(containsString("data-test-id=\"nav-my-accounts\""))))
                    .andExpect(content().string(
                            containsString("data-test-id=\"nav-admin-accounts\"")))
                    .andExpect(content().string(role.equals(Roles.CUSTOMER_SERVICE_MANAGER) ?
                            containsAdminTransactions : not(containsAdminTransactions)));
        } else {
            perform
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER})
    public void transferPageTransferLinkIsSecuredByRoles(final Roles role,
                                                         final boolean permitted)
            throws Exception {

        final ResultActions perform = mockMvc.perform(
                get("/admin/transfer")
                        .with(user("user").roles(role.name())));

        final Matcher<String> containsAdminTransactions =
                containsString("data-test-id=\"nav-admin-transactions\"");

        if (permitted) {


            perform
                    .andExpect(content().string(containsAdminTransactions));
        } else {
            perform
                    .andExpect(content().string(not(containsAdminTransactions)));
        }
    }
}
