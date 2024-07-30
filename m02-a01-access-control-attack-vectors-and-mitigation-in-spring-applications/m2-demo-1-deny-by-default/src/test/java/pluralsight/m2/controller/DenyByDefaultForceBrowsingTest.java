package pluralsight.m2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DenyByDefaultForceBrowsingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void redirectToLoginPageWhenViewingAccountsWithoutLogin() throws Exception {
        verifyLoginRedirect(get("/admin/accounts"));
    }

    @Test
    public void redirectToLoginPageWhenViewingTransfersWithoutLogin() throws Exception {
        verifyLoginRedirect(get("/admin/transfers"));
    }

    @Test
    public void redirectToLoginPageWhenCreatingTransfersWithoutLogin() throws Exception {
        verifyLoginRedirect(post("/admin/transfers").with(csrf()));
    }

    @Test
    public void redirectToLoginPageWhenRequestingAnyRandomUrlWithoutLogin() throws Exception {
        verifyLoginRedirect(get("/" + UUID.randomUUID()));
    }

    @Test
    @WithMockUser
    public void permitViewingAccountsWhenLoggedIn() throws Exception {
        mockMvc.perform(get("/admin/accounts"))
                .andExpect(status().is2xxSuccessful());
    }

    private void verifyLoginRedirect(final MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(redirectedUrl("http://localhost/login"));
    }
}
