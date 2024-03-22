package pluralsight.m2.controller;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pluralsight.m2.repository.AccountRepository;
import pluralsight.m2.repository.TestDataFactory;
import pluralsight.m2.security.Roles;
import pluralsight.m2.util.AllowedRoles;
import pluralsight.m2.util.RoleBasedArgumentsProvider;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedAccessToEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER})
    public void myAccountsIsSecured(final Roles role, final boolean permitted) throws Exception {

        final ResultActions perform = mockMvc.perform(
                get("/my-accounts")
                        .with(user("user").roles(role.name())));

        if (permitted) {
            perform
                    .andExpect(status().is(200))
                    .andExpect(content().string(containsString("data-test-id=\"nav-my-accounts\"")))
                    .andExpect(content().string(not(containsString("data-test-id=\"nav-admin-accounts\""))))
                    .andExpect(content().string(not(containsString("data-test-id=\"nav-admin-transactions\""))));
        } else {
            perform
                    .andExpect(status().is(403));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER})
    public void myAccountTransactionsIsSecured(final Roles role, final boolean permitted) throws Exception {

        accountRepository.save(TestDataFactory.generateAccount("user", 0));

        final ResultActions perform = mockMvc.perform(
                get("/accounts/0/transactions")
                        .with(user("user").roles(role.name())));

        if (permitted) {
            perform
                    .andExpect(status().is(200))
                    .andExpect(content().string(containsString("data-test-id=\"nav-my-accounts\"")))
                    .andExpect(content().string(not(containsString("data-test-id=\"nav-admin-accounts\""))))
                    .andExpect(content().string(not(containsString("data-test-id=\"nav-admin-transactions\""))));
        } else {
            perform
                    .andExpect(status().is(403));
        }

        accountRepository.deleteAll();
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER})
    public void adminAccountsPageIsSecuredByRoles(final Roles role, final boolean permitted) throws Exception {

        final ResultActions perform = mockMvc.perform(
                        get("/admin/accounts")
                                .with(user("user").roles(role.name()).authorities(role.getGrantedAuthorities())))
                .andExpect(status().is(permitted ? 200 : 403));

        if (permitted) {

            perform
                    .andExpect(status().is(200))
                    .andExpect(content().string(not(containsString("data-test-id=\"nav-my-accounts\""))))
                    .andExpect(content().string(containsString("data-test-id=\"nav-admin-accounts\"")))
                    .andExpect(content().string(containsString("data-test-id=\"nav-admin-transactions\"")));
        } else {
            perform
                    .andExpect(status().is(403));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER, Roles.CUSTOMER_SERVICE})
    public void transferPageIsSecuredByRoles(final Roles role, final boolean permitted) throws Exception {

        final ResultActions perform = mockMvc.perform(
                get("/admin/transfer")
                        .with(user("user").roles(role.name()).authorities(role.getGrantedAuthorities())));

        if (permitted) {

            perform
                    .andExpect(status().is(200))
                    .andExpect(content().string(not(containsString("data-test-id=\"nav-my-accounts\""))))
                    .andExpect(content().string(containsString("data-test-id=\"nav-admin-accounts\"")))
                    .andExpect(content().string(containsString("data-test-id=\"nav-admin-transactions\"")));
        } else {
            perform
                    .andExpect(status().is(403));
        }
    }
}