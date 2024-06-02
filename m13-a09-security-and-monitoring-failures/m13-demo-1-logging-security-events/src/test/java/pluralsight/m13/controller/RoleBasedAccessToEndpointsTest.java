package pluralsight.m13.controller;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pluralsight.m13.domain.Employee;
import pluralsight.m13.repository.AccountRepository;
import pluralsight.m13.repository.EmployeeRepository;
import pluralsight.m13.security.Roles;
import pluralsight.m13.util.AllowedRoleAndResources;
import pluralsight.m13.util.AllowedRolesAndResources;
import pluralsight.m13.util.AllowedRolesAndResourcesArgumentProvider;
import pluralsight.m13.util.TestAccountBuilder;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedAccessToEndpointsTest {

    public static final String EMPLOYEES_MENU_ITEM =
            "data-test-id=\"nav-employees\"";
    public static final String ACCOUNTS_MENU_ITEM =
            "data-test-id=\"nav-admin-accounts\"";
    public static final String TRANSFERS_MENU_ITEM =
            "data-test-id=\"nav-admin-transfers\"";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesAndResourcesArgumentProvider.class)
    @AllowedRolesAndResources(
            allResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE_MANAGER,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.SENIOR_VICE_PRESIDENT,
                            visibleResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM,
                                    TRANSFERS_MENU_ITEM})})
    public void adminAccountsPageIsSecuredByRoles(final Authentication authentication,
                                                  final Set<String> permittedDataTestIds,
                                                  final Set<String> notPermittedDataTestIds,
                                                  boolean permitted) throws Exception {

        verifyAccess(get("/admin/accounts"),
                authentication,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesAndResourcesArgumentProvider.class)
    @AllowedRolesAndResources(
            allResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE_MANAGER,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.SENIOR_VICE_PRESIDENT,
                            visibleResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM,
                                    TRANSFERS_MENU_ITEM})})
    public void accountPageIsSecuredByRoles(final Authentication authentication,
                                            final Set<String> permittedDataTestIds,
                                            final Set<String> notPermittedDataTestIds,
                                            boolean permitted) throws Exception {

        accountRepository.save(TestAccountBuilder.testAccountBuilder().build());

        verifyAccess(get("/admin/accounts/code"),
                authentication,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesAndResourcesArgumentProvider.class)
    @AllowedRolesAndResources(
            allResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE_MANAGER,
                            visibleResourceIds = {ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.SENIOR_VICE_PRESIDENT,
                            visibleResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM,
                                    TRANSFERS_MENU_ITEM})})
    public void transferPageIsSecuredByRoles(final Authentication authentication,
                                             final Set<String> permittedDataTestIds,
                                             final Set<String> notPermittedDataTestIds,
                                             boolean permitted) throws Exception {

        verifyAccess(get("/admin/transfer"),
                authentication,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesAndResourcesArgumentProvider.class)
    @AllowedRolesAndResources(
            allResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE_MANAGER),
                    @AllowedRoleAndResources(role = Roles.CUSTOMER_SERVICE),
                    @AllowedRoleAndResources(role = Roles.SENIOR_VICE_PRESIDENT)})
    public void performTransferIsSecuredByRoles(final Authentication authentication,
                                                final Set<String> permittedDataTestIds,
                                                final Set<String> notPermittedDataTestIds,
                                                boolean permitted) throws Exception {

        accountRepository.save(TestAccountBuilder.testAccountBuilder().accountCode("ABC").build());

        accountRepository.save(TestAccountBuilder.testAccountBuilder().accountCode("DEF").build());

        verifyAccess(post("/admin/transfer").param("fromAccountCode", "ABC")
                        .param("toAccountCode", "DEF").param("amount", "100").with(csrf()),
                authentication,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted, 302);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesAndResourcesArgumentProvider.class)
    @AllowedRolesAndResources(
            allResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRoleAndResources(role = Roles.HUMAN_RESOURCES,
                            visibleResourceIds = {EMPLOYEES_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.SENIOR_VICE_PRESIDENT,
                            visibleResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM,
                                    TRANSFERS_MENU_ITEM})})
    public void employeesPageIsSecuredByRoles(final Authentication authentication,
                                              final Set<String> permittedDataTestIds,
                                              final Set<String> notPermittedDataTestIds,
                                              boolean permitted) throws Exception {

        verifyAccess(get("/employees"),
                authentication,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    @ParameterizedTest
    @ArgumentsSource(AllowedRolesAndResourcesArgumentProvider.class)
    @AllowedRolesAndResources(
            allResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM, TRANSFERS_MENU_ITEM},
            allowed = {
                    @AllowedRoleAndResources(role = Roles.HUMAN_RESOURCES,
                            visibleResourceIds = {EMPLOYEES_MENU_ITEM}),
                    @AllowedRoleAndResources(role = Roles.SENIOR_VICE_PRESIDENT,
                            visibleResourceIds = {EMPLOYEES_MENU_ITEM, ACCOUNTS_MENU_ITEM,
                                    TRANSFERS_MENU_ITEM})})
    public void employeePageIsSecuredByRoles(final Authentication authentication,
                                             final Set<String> permittedDataTestIds,
                                             final Set<String> notPermittedDataTestIds,
                                             boolean permitted) throws Exception {

        final UUID employeeId = UUID.randomUUID();
        employeeRepository.save(Employee.builder().employeeId(employeeId).build());

        verifyAccess(get("/employees/" + employeeId),
                authentication,
                permittedDataTestIds,
                notPermittedDataTestIds,
                permitted);
    }

    private void verifyAccess(final MockHttpServletRequestBuilder requestBuilder,
                              final Authentication authentication,
                              final Set<String> permittedDataTestIds,
                              final Set<String> notPermittedDataTestIds,
                              final boolean permitted,
                              final int permittedStatusCode) throws Exception {

        final ResultActions result =
                mockMvc.perform(requestBuilder.with(authentication(authentication)));

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
                              final Authentication authentication,
                              final Set<String> permittedDataTestIds,
                              final Set<String> notPermittedDataTestIds,
                              final boolean permitted) throws Exception {

        verifyAccess(requestBuilder, authentication, permittedDataTestIds,
                notPermittedDataTestIds, permitted, 200);

    }
}