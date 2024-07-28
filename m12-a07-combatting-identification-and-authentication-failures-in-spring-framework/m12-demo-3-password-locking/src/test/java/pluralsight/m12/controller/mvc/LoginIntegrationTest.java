package pluralsight.m12.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class LoginIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(LoginIntegrationTest.class);
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CompromisedPasswordChecker compromisedPasswordChecker;

    @BeforeEach
    public void beforeEach() {
        when(compromisedPasswordChecker.check(any()))
                .thenReturn(new CompromisedPasswordDecision(false));
    }

    @Test
    public void testLoginWithCompromisedPassword() throws Exception {
        when(compromisedPasswordChecker.check("password"))
                .thenReturn(new CompromisedPasswordDecision(true));

        final String username = createTestUser();

        login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reset-password"));
    }

    @Test
    public void testLogInWithCorrectPassword() throws Exception {

        final String username = createTestUser();

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        mockMvc.perform(get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(redirectedUrl("/my-accounts"));
    }

    @Test
    public void testLogInWithWrongPassword() throws Exception {

        final String username = createTestUser();

        final MvcResult mvcResult = login(username, "wrong")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();

        mockMvc.perform(get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    private String createTestUser() {

        final String username = "user-" + UUID.randomUUID();

        userRepository.saveUser(pluralsight.m12.domain.User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode("password"))
                .build());

        return username;
    }

    @Test
    public void accountLockedAfterThreeFailedAttemptsAndUnlockedAfterOneMinute()
            throws Exception {
        final String username = createTestUser();

        for (int i = 0; i < 3; i++) {
            login(username, "wrong")
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error"));
        }

        login(username, "password")
                .andExpect(redirectedUrl("/login?error"));

        final User user = userRepository.getUser(username).orElseThrow();
        user.setLastFailedLoginTime(LocalDateTime.now().minusMinutes(10));

        final ResultActions resultActions = login(username, "password")
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/")
                .session((MockHttpSession) resultActions.andReturn().getRequest().getSession()))
                .andExpect(redirectedUrl("/my-accounts"));
    }


    private ResultActions login(final String username, final String password)
            throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", username)
                .param("password", password)
                .with(csrf()));
    }
}

