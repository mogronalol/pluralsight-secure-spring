package pluralsight.m3.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pluralsight.m3.domain.Account;
import pluralsight.m3.repository.AccountRepository;
import pluralsight.m3.security.Roles;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedAccessToEndpointsTest {

    public static final String MY_ACCOUNTS_MENU_ITEM = "data-test-id=\"nav-my-accounts\"";
    public static final String ACCOUNTS_MENU_ITEM = "data-test-id=\"nav-admin-accounts\"";
    public static final String TRANSFERS_MENU_ITEM = "data-test-id=\"nav-admin-transfers\"";

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
    @AllowedRoles(
            allResourceIds = {MY_ACCOUNTS_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {@AllowedRole(role = Roles.CUSTOMER,
                    visibleResourceIds = {MY_ACCOUNTS_MENU_ITEM})})
    public void verifyRoleBasedAccessToMyAccountsPage(Roles role,
                                                      final Set<String> permittedDataTestIds,
                                                      final Set<String> notPermittedDataTestIds,
                                                      boolean permitted) throws Exception {

        verifyAccess(
                get("/my-accounts"),
                role,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles(
            allResourceIds = {MY_ACCOUNTS_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRole(role = Roles.CUSTOMER_SERVICE_MANAGER,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM}),
                    @AllowedRole(role = Roles.CUSTOMER_SERVICE,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM})})
    public void verifyRoleBasedAccessToAdminAccountsPage(Roles role,
                                                         final Set<String> permittedDataTestIds,
                                                         final Set<String> notPermittedDataTestIds,
                                                         boolean permitted) throws Exception {

        verifyAccess(
                get("/admin/accounts"),
                role,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles(
            allResourceIds = {MY_ACCOUNTS_MENU_ITEM, ACCOUNTS_MENU_ITEM,
                    TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRole(role = Roles.CUSTOMER_SERVICE_MANAGER,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM,
                                    TRANSFERS_MENU_ITEM})})
    public void verifyRoleBasedAccessToAdminTransfersPage(Roles roles,
                                                          final Set<String> permittedDataTestIds,
                                                          final Set<String> notPermittedDataTestIds,
                                                          boolean permitted) throws Exception {

        verifyAccess(
                get("/admin/transfer"),
                roles,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentProvider.class)
    @AllowedRoles(
            allResourceIds = {MY_ACCOUNTS_MENU_ITEM, ACCOUNTS_MENU_ITEM,
                    TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRole(role = Roles.CUSTOMER_SERVICE_MANAGER,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM,
                                    TRANSFERS_MENU_ITEM})})
    public void verifyRoleBasedAccessToAdminTransfers(Roles roles,

                                                      final Set<String> permittedDataTestIds,
                                                      final Set<String> notPermittedDataTestIds,
                                                      boolean permitted) throws Exception {

        accountRepository.save(Account.builder().accountCode("account1").build());
        accountRepository.save(Account.builder().accountCode("account2").build());

        verifyAccess(
                get("/admin/transfer"),
                roles,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);

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

    private void verifyAccess(final MockHttpServletRequestBuilder requestBuilder,
                              final Roles role,
                              final Set<String> permittedDataTestIds,
                              final Set<String> notPermittedDataTestIds,
                              final boolean permitted,
                              final int permittedStatusCode) throws Exception {

        final ResultActions result =
                mockMvc.perform(requestBuilder.with(user("username").roles(role.name())));

        if (permitted) {

            result.andExpect(status().is(permittedStatusCode));

            for (String p : permittedDataTestIds) {
                result.andExpect(content().string(containsString(p)));
            }

            for (String p : notPermittedDataTestIds) {
                result.andExpect(content().string(not(containsString(p))));
            }
        } else {
            result.andExpect(status().isForbidden());
        }
    }

    private void verifyAccess(final MockHttpServletRequestBuilder requestBuilder,
                              final Roles roles,
                              final Set<String> permittedDataTestIds,
                              final Set<String> notPermittedDataTestIds,
                              final boolean permitted) throws Exception {

        verifyAccess(requestBuilder, roles, permittedDataTestIds,
                notPermittedDataTestIds, permitted, 200);

    }
}
