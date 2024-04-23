package pluralsight.m2.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pluralsight.m2.domain.Account;
import pluralsight.m2.repository.AccountRepository;
import pluralsight.m2.security.Roles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedAccessToEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void cleanUp() {
        accountRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles(Roles.CUSTOMER)
    public void verifyRoleBasedAccessToMyAccountsPage(Roles roles, boolean permitted)
            throws Exception {

        final ResultActions perform = mockMvc.perform(get("/my-accounts")
                .with(user("user").roles(roles.name()))
        );

        if (permitted) {
            perform
                    .andExpect(status().isOk());
        } else {
            perform
                    .andExpect(status().isForbidden());
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
                    .andExpect(status().isOk());
        } else {
            perform
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER})
    public void verifyRoleBasedAccessToAdminTransfersPage(Roles roles, boolean permitted)
            throws Exception {

        final ResultActions perform = mockMvc.perform(get("/admin/transfer")
                .with(user("user").roles(roles.name()))
        );

        if (permitted) {
            perform
                    .andExpect(status().isOk());
        } else {
            perform
                    .andExpect(status().isForbidden());
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER})
    public void verifyRoleBasedAccessToAdminTransfers(Roles roles, boolean permitted)
            throws Exception {

        accountRepository.save(Account.builder().accountCode("account1").build());
        accountRepository.save(Account.builder().accountCode("account2").build());

        final ResultActions perform = mockMvc.perform(
                post("/admin/transfer")
                        .param("fromAccountCode", "account1")
                        .param("toAccountCode", "account2")
                        .param("amount", "123")
                        .with(csrf())
                        .with(user("user").roles(roles.name()))
        );

        if (permitted) {
            perform
                    .andExpect(status().is3xxRedirection());
        } else {
            perform
                    .andExpect(status().isForbidden());
        }
    }
}
