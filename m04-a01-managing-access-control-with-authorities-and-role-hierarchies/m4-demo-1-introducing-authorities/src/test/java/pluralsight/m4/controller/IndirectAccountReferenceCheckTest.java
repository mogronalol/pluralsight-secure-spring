package pluralsight.m4.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m4.domain.Account;
import pluralsight.m4.repository.AccountRepository;
import pluralsight.m4.repository.TestDataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class  IndirectAccountReferenceCheckTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    private Account user1Account1;
    private Account user1Account2;
    private Account user2Account1;
    private Account user2Account2;

    @BeforeEach
    public void setUp() {

        accountRepository.deleteAll();

        user1Account1 = TestDataFactory.generateAccount("user-1", 0);
        user1Account2 = TestDataFactory.generateAccount("user-1", 1);
        user2Account1 = TestDataFactory.generateAccount("user-2", 0);
        user2Account2 = TestDataFactory.generateAccount("user-2", 1);

        accountRepository.save(user1Account1);
        accountRepository.save(user1Account2);
        accountRepository.save(user2Account1);
        accountRepository.save(user2Account2);
    }

    @Test
    @WithMockUser(username = "user-1", roles = {"CUSTOMER"})
    public void shouldReturnUser1AccountsByIndex() throws Exception {

        mockMvc.perform(get("/accounts/0/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("account", user1Account1))
                .andExpect(view().name("transactions"));

        mockMvc.perform(get("/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("account", user1Account2))
                .andExpect(view().name("transactions"));
    }

    @Test
    @WithMockUser(username = "user-2", roles = {"CUSTOMER"})
    public void shouldReturnUser2AccountsByIndex() throws Exception {

        mockMvc.perform(get("/accounts/0/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("account", user2Account1))
                .andExpect(view().name("transactions"));

        mockMvc.perform(get("/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attribute("account", user2Account2))
                .andExpect(view().name("transactions"));
    }
}