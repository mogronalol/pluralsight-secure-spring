package pluralsight.m14.controller;

import io.specto.hoverfly.junit.core.Hoverfly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m14.domain.Account;

import java.math.BigDecimal;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.HttpBodyConverter.json;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Hoverfly hoverfly;

    @BeforeEach
    public void beforeEach() {
        Account[] adminResponse = new Account[]{Account.builder()
                .type("current")
                .balance(BigDecimal.valueOf(100))
                .accountCode("code")
                .index(0)
                .displayName("Admin account")
                .build()};

        Account[] regularResponse = new Account[]{Account.builder()
                .type("current")
                .balance(BigDecimal.valueOf(100))
                .accountCode("code")
                .index(0)
                .displayName("Current account")
                .build()};

        hoverfly.simulate(
                dsl(service("http://internal-service.com")
                        .get("/accounts")
                        .queryParam("username", "user")
                        .queryParam("accountType", "CURRENT")
                        .queryParam("admin", true)
                        .willReturn(success(json(adminResponse)))
                        .get("/accounts")
                        .queryParam("username", "user")
                        .queryParam("accountType", "CURRENT")
                        .willReturn(success(json(regularResponse)))
                )
        );
    }

    @Test
    @WithMockUser(username = "user")
    public void shouldNotBeAbleToSeeAdminAccounts() throws Exception {
        mockMvc.perform(get("/")
                        .queryParam("accountType", "CURRENT&admin=true"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("Admin account"))));
    }

    @Test
    @WithMockUser(username = "user")
    public void shouldBeAbleToSeeCurrentAccounts() throws Exception {

        mockMvc.perform(get("/").queryParam("accountType", "CURRENT"))
                .andExpect(content().string((containsString("Current account"))));
    }

}
