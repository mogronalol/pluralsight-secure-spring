package pluralsight.m14.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pluralsight.m14.repository.AccountRepository;
import pluralsight.m14.repository.TestDataFactory;
import pluralsight.m14.domain.Account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IndirectAccountReferenceCheckTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    private Account account1;
    private Account account2;
    private Account account3;
    private Account account4;

    @BeforeEach
    public void setUp() {
        accountRepository.deleteAll();
        account1 = TestDataFactory.generateAccount("user-1", 0);
        account2 = TestDataFactory.generateAccount("user-1", 1);
        account3 = TestDataFactory.generateAccount("user-2", 0);
        account4 = TestDataFactory.generateAccount("user-2", 1);

        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);
        accountRepository.save(account4);
    }

    @Test
    @WithMockUser(username = "user-1")
    public void shouldReturnUser1AccountsByIndex() throws Exception {
        mockMvc.perform(get("/accounts/0/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("account", account1))
                .andExpect(view().name("transactions"));

        mockMvc.perform(get("/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("account", account2))
                .andExpect(view().name("transactions"));
    }

    @Test
    @WithMockUser(username = "user-2")
    public void shouldReturnUser2AccountsByIndex() throws Exception {
        mockMvc.perform(get("/accounts/0/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("account", account3))
                .andExpect(view().name("transactions"));

        mockMvc.perform(get("/accounts/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("account", account4))
                .andExpect(view().name("transactions"));
    }
}
