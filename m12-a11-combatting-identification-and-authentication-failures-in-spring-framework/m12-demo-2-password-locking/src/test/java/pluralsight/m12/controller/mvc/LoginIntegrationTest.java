package pluralsight.m12.controller.mvc;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import pluralsight.m12.service.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class LoginIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CompromisedPasswordChecker compromisedPasswordChecker;

    @BeforeEach
    public void setUp() {
        when(compromisedPasswordChecker.check(any())).thenReturn(
                new CompromisedPasswordDecision(false));
    }

    @Test
    public void testLogInWithCorrectPassword() throws Exception {

        final String username = createTestUser("password");

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        mockMvc.perform(get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                // Preserve
                .andExpect(redirectedUrl("/my-accounts"));
    }

    @Test
    public void testLogInWithWrongPassword() throws Exception {

        final String username = createTestUser("password");

        final MvcResult mvcResult = login(username, "wrong")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andReturn();

        mockMvc.perform(get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                // Preserve
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    public void testLogInWithCompromisedPassword() throws Exception {

        when(compromisedPasswordChecker.check("password")).thenReturn(
                new CompromisedPasswordDecision(true));

        final String username = createTestUser("password");

        final HttpSession session = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reset-password"))
                .andReturn()
                .getRequest()
                .getSession();

        assertThat(session.getAttribute("compromisedPassword"))
                .isEqualTo(true);

        MvcResult redirectedResult = mockMvc.perform(get("/reset-password")
                        .session((MockHttpSession) session))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(session.getAttribute("compromisedPassword")).isNull();

        String content = redirectedResult.getResponse().getContentAsString();
        assertThat(content).contains(
                "The password you tried to use has appeared in data breaches on other sites," +
                " not ours");
    }

    private String createTestUser(final String password) {

        final String username = "user-" + UUID.randomUUID();

        userRepository.saveUser(pluralsight.m12.domain.User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build());

        return username;
    }

    @Test
    public void testFailedLoginAttemptRecording() throws Exception {
        final String username = createTestUser("password");

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .param("username", username)
                            .param("password", "wrongPassword")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error"));
        }

        User user = userRepository.getUser(username).orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
    }

    @Test
    public void testAccountLockAfterFailedAttempts() throws Exception {
        final String username = createTestUser("password");

        for (int i = 0; i < UserService.MAX_LOGIN_BEFORE_LOCK; i++) {
            mockMvc.perform(MockMvcRequestBuilders.post("/login")
                            .param("username", username)
                            .param("password", "wrongPassword")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?error"));
        }

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .param("username", username)
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=locked"))
                .andReturn();

        HttpSession session = result.getRequest().getSession();
        assertThat(session.getAttribute("locked")).isEqualTo(true);
    }

    @Test
    public void testAccountUnlockedAfterOneMinute() throws Exception {
        final String username = createTestUser("password");

        final User user = userRepository.getUser(username).orElseThrow();
        user.setFailedLoginAttempts(10);
        user.setLastFailedLoginTime(LocalDateTime.now().minusMinutes(2));
        userRepository.saveUser(user);

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        mockMvc.perform(get("/")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())) //
                // Preserve
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

