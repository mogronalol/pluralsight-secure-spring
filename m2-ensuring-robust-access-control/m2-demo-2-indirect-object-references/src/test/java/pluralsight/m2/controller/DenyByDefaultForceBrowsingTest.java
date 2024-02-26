package pluralsight.m2.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
    @WithMockUser
    public void permitViewingAccountsWhenLoggedIn() throws Exception {

        mockMvc.perform(get("/my-accounts"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void redirectToLoginPageWhenViewingAnyPageWithoutLogin() throws Exception {

        mockMvc.perform(get("/" + UUID.randomUUID()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().stringValues("Location", "http://localhost/login"));
    }
}