package pluralsight.m5.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m5.security.Roles;
import pluralsight.m5.util.WithMockRole;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DenyByDefaultForceBrowsingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void redirectToLoginPageWhenViewingAccountsWithoutLogin() throws Exception {

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues("Location", "http://localhost/login"));
    }

    @Test
    public void redirectToTransferPageWhenViewingTransfersWithoutLogin() throws Exception {

        mockMvc.perform(get("/admin/transfers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues("Location", "http://localhost/login"));
    }

    @Test
    @WithMockRole(Roles.CUSTOMER_SERVICE)
    public void permitViewingAccountsWhenLoggedIn() throws Exception {

        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockRole(Roles.CUSTOMER_SERVICE_MANAGER)
    public void permitViewingTransfersWhenLoggedIn() throws Exception {

        mockMvc.perform(get("/admin/transfer"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void redirectToLoginPageWhenViewingAnyPageWithoutLogin() throws Exception {

        mockMvc.perform(get("/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues("Location", "http://localhost/login"));
    }
}