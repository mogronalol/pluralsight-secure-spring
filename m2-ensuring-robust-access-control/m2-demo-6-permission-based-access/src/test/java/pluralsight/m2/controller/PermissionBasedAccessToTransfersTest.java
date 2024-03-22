package pluralsight.m2.controller;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pluralsight.m2.domain.Account;
import pluralsight.m2.repository.AccountRepository;
import pluralsight.m2.repository.TestDataFactory;
import pluralsight.m2.security.Roles;
import pluralsight.m2.util.AllowedRoles;
import pluralsight.m2.util.RoleBasedArgumentsProvider;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PermissionBasedAccessToTransfersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    private Account account1;
    private Account account2;

    @BeforeEach
    public void setUp() {

        accountRepository.deleteAll();

        account1 = TestDataFactory.generateAccount("user-1", 0);
        account2 = TestDataFactory.generateAccount("user-1", 1);

        accountRepository.save(account1);
        accountRepository.save(account2);
    }

    @AfterEach
    public void afterEach() {
        accountRepository.deleteAll();
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE, Roles.CUSTOMER_SERVICE_MANAGER})
    public void transfersCanOnlyBeDoneByCustomerService(final Roles role, final boolean permitted) throws Exception {

        final ResultActions perform = mockMvc.perform(post("/admin/transfer")
                .param("fromAccountCode", account1.getAccountCode())
                .param("toAccountCode", account2.getAccountCode())
                .param("amount", "999.99")
                .with(user("user").roles(role.name()).authorities(role.getGrantedAuthorities()))
                .with(csrf()));

        if (permitted) {
            perform
                    .andExpect(status().is(302))
                    .andExpect(redirectedUrl("/admin/transfer"))
                    .andExpect(flash().attributeExists("completed"));
        } else {
            perform.andExpect(status().is(403));
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RoleBasedArgumentsProvider.class)
    @AllowedRoles({Roles.CUSTOMER_SERVICE_MANAGER})
    public void largeTransfersCanOnlyBeDoneByCustomerServiceManagers(final Roles role, final boolean permitted) throws Exception {

        final ResultActions perform = mockMvc.perform(post("/admin/transfer")
                .param("fromAccountCode", account1.getAccountCode())
                .param("toAccountCode", account2.getAccountCode())
                .param("amount", "1000")
                .with(user("user").roles(role.name()).authorities(role.getGrantedAuthorities()))
                .with(csrf()));

        if (permitted) {
            perform
                    .andExpect(status().is(302))
                    .andExpect(redirectedUrl("/admin/transfer"))
                    .andExpect(flash().attributeExists("completed"));
        } else {
            perform.andExpect(status().is(403));
        }
    }
}