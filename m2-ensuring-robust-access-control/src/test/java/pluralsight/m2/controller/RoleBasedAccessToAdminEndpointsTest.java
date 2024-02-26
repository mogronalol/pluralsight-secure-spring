package pluralsight.m2.controller;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m2.security.Roles;
import pluralsight.m2.controller.util.AllowedRoles;
import pluralsight.m2.controller.util.RoleBasedArgumentsProvider;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedAccessToAdminEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER})
    public void accountsPageIsSecuredByRoles(final Roles role, final boolean permitted) throws Exception {

        mockMvc.perform(
                        get("/admin/accounts")
                                .with(user("user").roles(role.name())))
                .andExpect(status().is(permitted ? 200 : 401));
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER})
    public void transferPageIsSecuredByRoles(final Roles role, final boolean permitted) throws Exception {

        mockMvc.perform(
                        get("/admin/transfers")
                                .with(user("user").roles(role.name())))
                .andExpect(status().is(permitted ? 200 : 401));
    }
}