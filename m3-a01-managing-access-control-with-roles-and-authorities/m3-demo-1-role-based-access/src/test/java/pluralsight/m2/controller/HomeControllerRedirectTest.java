package pluralsight.m2.controller;

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
    @WithMockUser(roles = {"CUSTOMER_SERVICE"})
    public void whenCustomerService_thenRedirectToAdminAccounts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"));
    }

    @Test
    @WithMockUser(roles = {"CUSTOMER_SERVICE_MANAGER"})
    public void whenAdmin_thenRedirectToAdminAccounts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/accounts"));
    }

    @Test
    @WithMockUser(roles = {"CUSTOMER"})
    public void whenCustomer_thenRedirectToMyAccounts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-accounts"));
    }

    @Test
    @WithMockUser()
    public void whenUnknownUser_thenRedirectToMyAccounts() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isForbidden());
    }
}
