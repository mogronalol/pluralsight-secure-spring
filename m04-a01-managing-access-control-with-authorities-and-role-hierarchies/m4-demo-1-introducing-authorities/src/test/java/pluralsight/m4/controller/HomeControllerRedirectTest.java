package pluralsight.m4.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerRedirectTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CUSTOMER_SERVICE")
    public void whenCustomerSupportThenRedirectToAdminAccounts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"));

    }

    @Test
    @WithMockUser(roles = "CUSTOMER_SERVICE_MANAGER")
    public void whenCustomerServiceManagerThenRedirectToAdminAccounts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"));

    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    public void whenCustomerThenRedirectToMyAccounts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-accounts"));

    }

    @Test
    @WithMockUser
    public void whenNoRoleThenForbidden() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isForbidden());

    }
}
