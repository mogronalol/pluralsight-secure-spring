package pluralsight.m9.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CSRFProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CUSTOMER_SERVICE_MANAGER")
    public void verifyTransfersNotPossibleWithoutCSRF() throws Exception {
        mockMvc.perform(post("/admin/transfer")
                .param("fromAccountCode", "1000000")
                .param("toAccountCode", "1000001")
                .param("amount", "100")
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER_SERVICE_MANAGER")
    public void verifyTransfersPossibleWithCSRF() throws Exception {
        mockMvc.perform(post("/admin/transfer")
                        .param("fromAccountCode", "1000000")
                        .param("toAccountCode", "1000001")
                        .param("amount", "100")
                        .with(csrf()))
                .andExpect(redirectedUrl("/admin/transfer"));
    }
}
