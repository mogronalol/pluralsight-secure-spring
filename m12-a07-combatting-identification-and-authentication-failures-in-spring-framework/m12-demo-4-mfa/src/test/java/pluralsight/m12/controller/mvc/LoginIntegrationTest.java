package pluralsight.m12.controller.mvc;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import pluralsight.m12.domain.User;
import pluralsight.m12.repository.UserRepository;
import pluralsight.m12.service.MfaClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private MfaClient mfaClient;

    @BeforeEach
    public void setUp() {
        when(compromisedPasswordChecker.check(any())).thenReturn(
                new CompromisedPasswordDecision(false));
    }

    @Test
    public void testLogInWithCorrectPasswordAndCorrectToken() throws Exception {

        final String username = createTestUser();

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/otp"))
                .andReturn();

        mockMvc.perform(get("/login/otp")
                        .session((MockHttpSession) mvcResult.getRequest().getSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter your OTP")));

        mockMvc.perform(post("/login/otp")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())
                        .param("otp", verifyAndRetrieveSentOtp(username))
                        .with(csrf())
                )
                .andExpect(redirectedUrl("/my-accounts"));
    }

    @Test
    public void testLogInWithCorrectPasswordAndWrongToken() throws Exception {

        final String username = createTestUser();

        final MvcResult mvcResult = login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login/otp"))
                .andReturn();

        mockMvc.perform(get("/login/otp")
                        .session((MockHttpSession) mvcResult.getRequest().getSession()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter your OTP")));

        for (int i = 0; i < 2; i++) {

            mockMvc.perform(post("/login/otp")
                            .session((MockHttpSession) mvcResult.getRequest().getSession())
                            .param("otp", "wrong")
                            .with(csrf())
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Invalid or expired token, try " +
                                                               "again.")));
        }

        mockMvc.perform(post("/login/otp")
                        .session((MockHttpSession) mvcResult.getRequest().getSession())
                        .param("otp", "wrong")
                        .with(csrf())
                )
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("locked", true));

        User user = userRepository.getUser(username).orElseThrow();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
    }

    private String verifyAndRetrieveSentOtp(final String username) {
        final ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);
        verify(mfaClient).sendOtpEmail(eq(username), tokenCaptor.capture());
        assertThat(tokenCaptor.getValue()).isNotNull();
        return tokenCaptor.getValue();
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
    public void testLogInWithCompromisedPassword() throws Exception {

        when(compromisedPasswordChecker.check("password")).thenReturn(
                new CompromisedPasswordDecision(true));

        final String username = createTestUser();

        login(username, "password")
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reset-password"))
                .andReturn();
    }

    private String createTestUser() {

        final String username = "user-" + UUID.randomUUID();

        userRepository.saveUser(pluralsight.m12.domain.User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode("password"))
                .build());

        return username;
    }

    private ResultActions login(final String username, final String password)
            throws Exception {
        return mockMvc.perform(post("/login")
                .param("username", username)
                .param("password", password)
                .with(csrf()));
    }
}

