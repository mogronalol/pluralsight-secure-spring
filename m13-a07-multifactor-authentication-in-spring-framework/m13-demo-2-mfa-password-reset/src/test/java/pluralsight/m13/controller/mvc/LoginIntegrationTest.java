package pluralsight.m13.controller.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pluralsight.m13.domain.User;
import pluralsight.m13.repository.UserRepository;
import pluralsight.m13.security.MfaLoginClient;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @MockBean
    private MfaLoginClient mfaClient;

    @BeforeEach
    public void beforeEach() {
        when(compromisedPasswordChecker.check(any()))
                .thenReturn(new CompromisedPasswordDecision(false));
    }

    @Test
    public void shouldNotBeAbleToAccessLoginOtpWithoutPartialLogin() throws Exception {
        mockMvc.perform(get("/login/otp"))
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser
    public void shouldNotBeAbleToAccessLoginOtpWithoutFullLogin() throws Exception {
        mockMvc.perform(get("/login/otp"))
                .andExpect(status().isForbidden());
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

        login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/otp"));

        verify(mfaClient).sendLoginOtp(eq(username), anyString());
    }

    @Test
    public void alwaysRedirectToOTPWithPartialLogin() throws Exception {
        final String username = createTestUser();

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/otp"))
                .andReturn();

        final MockHttpSession session = (MockHttpSession) mvcResult.getRequest().getSession();

        mockMvc.perform(get("/")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/otp"));
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

    @Test
    public void testLoginWithCorrectPasswordAndWrongToken() throws Exception {
        final String username = createTestUser();

        final ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/otp"))
                .andReturn();

        verify(mfaClient).sendLoginOtp(eq(username), tokenCaptor.capture());

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/login/otp")
                    .session((MockHttpSession) mvcResult.getRequest().getSession())
                    .param("otp", "wrong")
                    .with(csrf()))
                    .andExpect(content().string(containsString("Invalid or expired token")));
        }

        mockMvc.perform(post("/login/otp")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())
                        .param("otp", "wrong")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login?locked"));

        mockMvc.perform(post("/login/otp")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())
                        .param("otp", tokenCaptor.getValue())
                        .with(csrf()))
                .andExpect(redirectedUrl("http://localhost/login"));

        login(username, "password")
                .andExpect(redirectedUrl("/login?error"));

        final User user = userRepository.getUser(username).orElseThrow();
        user.setLastFailedLoginTime(LocalDateTime.now().minusDays(1));

        login(username, "password")
                .andExpect(redirectedUrl("/login/otp"));
    }

    private String createTestUser() {

        final String username = "user-" + UUID.randomUUID();

        userRepository.saveUser(User.builder()
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

        login(username, "password")
                .andExpect(redirectedUrl("/login/otp"));
    }

    private ResultActions login(final String username, final String password)
            throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .param("username", username)
                .param("password", password)
                .with(csrf()));
    }
}

