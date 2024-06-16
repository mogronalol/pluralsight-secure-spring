package pluralsight.m3.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m3.repository.AccountRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleBasedAccessToEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @WithMockUser(roles = "CUSTOMER")
    public void customerShouldHaveAccessToMyAccountsPage() throws Exception {

        mockMvc.perform(get("/my-accounts"))
                    .andExpect(status().isOk());
    }

    @WithMockUser(roles = "CUSTOMER_SERVICE")
    public void customerServiceShouldNotHaveAccessToMyAccountsPage() throws Exception {

        mockMvc.perform(get("/my-accounts"))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "CUSTOMER_SERVICE_MANAGER")
    public void customerServiceManagerShouldNotHaveAccessToMyAccountsPage() throws Exception {

        mockMvc.perform(get("/my-accounts"))
                .andExpect(status().isForbidden());
    }
}
